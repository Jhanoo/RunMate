package com.runhwani.runmate.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.JsonValue;
import com.openai.models.ChatModel;
import com.openai.models.ResponseFormatJsonSchema;
import com.openai.models.ResponseFormatJsonSchema.JsonSchema;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.runhwani.runmate.dao.CurriculumDao;
import com.runhwani.runmate.dao.HistoryDao;
import com.runhwani.runmate.dto.request.curriculum.CurriculumCreateRequest;
import com.runhwani.runmate.exception.EntityNotFoundException;
import com.runhwani.runmate.model.Curriculum;
import com.runhwani.runmate.model.History;
import com.runhwani.runmate.model.Todo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurriculumServiceImpl implements CurriculumService {

    private final HistoryDao historyDao;
    private final CurriculumDao curriculumDao;

    // 1달 전 기록 조회용
    private static final int ONE_MONTH = 1;

    @Value("${openai.api-key}")
    private String API_KEY;

    @Override
    @Transactional
    public UUID generateCurriculum(UUID userId, CurriculumCreateRequest req) {
        // 1) 최근 1달 기록 조회
        OffsetDateTime since = OffsetDateTime.now().minusMonths(ONE_MONTH);
        List<History> histories = historyDao.findByUserIdSinceDate(userId, since);

        // 2) 프롬프트 생성
        String prompt = buildPrompt(histories, req);

        // 3) AI 호출 (스키마 적용)
        String aiJson = callOpenAiWithSchema(prompt);
        log.debug("========\nAI 대답: {}", aiJson);

        // 4) JSON 파싱 → Map<날짜, 단일 ToDo 문자열>
        Map<LocalDate, String> schedule = parseAiResponse(aiJson);

        // 5) 이전 커리큘럼 처리: 진행중이면 종료 처리 후 Todo 삭제
        Curriculum prev = curriculumDao.selectCurriculumByUserId(userId);
        if (prev != null) {
            curriculumDao.updateCurriculumIsFinished(prev.getCurriculumId());
            curriculumDao.deleteTodoList(userId);
        }

        // 6) 새로운 Curriculum 저장
        UUID curriculumId = UUID.randomUUID();
        Curriculum curriculum = Curriculum.builder()
                .curriculumId(curriculumId)
                .userId(userId)
                .marathonId(req.getMarathonId())
                .goalDist(req.getGoalDist())
                .goalDate(req.getGoalDate())
                .runExp(req.isRunExp())
                .distExp(req.getDistExp())
                .freqExp(req.getFreqExp())
                .build();
        curriculumDao.insertCurriculum(curriculum);

        // 7) Todo 저장
        schedule.forEach((date, content) -> {
            // content가 "<숫자>km"로 시작하면 isDone=false, 아니면 null
            // content에서 거리(km) 추출하여 거리 기반의 훈련인지 판단
            Boolean isDoneVal = content.matches("^.*?:\\s*(\\d+(?:\\.\\d+)?)(km|m).*") ? false : null;

            Todo todo = Todo.builder()
                    .todoId(UUID.randomUUID())
                    .curriculumId(curriculumId)
                    .userId(userId)
                    .content(content)
                    .isDone(isDoneVal)
                    .date(date.atStartOfDay(ZoneId.of("Asia/Seoul"))
                            .toOffsetDateTime())
                    .build();
            curriculumDao.insertTodo(todo);
        });

        return curriculumId;
    }


    /**
     * 커리큘럼 조회
     * 1) 커리큘럼 테이블에서 사용자 ID로 조회
     * 2) todo 테이블에서 curriculumId로 모든 할 일 불러오기
     * 3) 날짜별로 그룹핑 후 도메인 객체로 반환
     */
    @Override
    @Transactional(readOnly = true)
    public Curriculum getMyCurriculum(UUID userId) {
        Curriculum curriculum = curriculumDao.selectCurriculumByUserId(userId);
        if (curriculum == null) {
            throw new EntityNotFoundException("생성된 커리큘럼이 없습니다.");
        }
        return curriculum;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Todo> getTodoListByMonth(UUID userId, int year, int month) {
        // 조회 기간 계산 (Asia/Seoul 기준)
        ZoneId zone = ZoneId.of("Asia/Seoul");
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1);

        OffsetDateTime start = startDate.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime end = endDate.atStartOfDay(zone).toOffsetDateTime();

        return curriculumDao.selectTodoListByPeriod(userId, start, end);
    }

    @Override
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void finishCurriculaSchedule() {
        curriculumDao.updateCurriculumIsFinishedEveryDay();
        log.debug("커리큘럼의 isFinished를 갱신합니다.");
    }

    /**
     * 프롬프트 작성부
     */
    private String buildPrompt(List<History> histories, CurriculumCreateRequest req) {
        // 1. 서울 시간대로 오늘과 시작 날짜 계산
        ZoneId seoul = ZoneId.of("Asia/Seoul");
        LocalDate startDate = LocalDate.now(seoul);
        LocalDate goalDate = req.getGoalDate().toLocalDate();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;

        StringBuilder sb = new StringBuilder();

        // (A) 역할 및 목적
        sb.append("당신은 전문 마라톤 트레이너입니다.\n");
        sb.append("사용자의 정보를 바탕으로 과학적 근거에 기반한 맞춤형 마라톤 훈련 계획을 생성해주세요.\n\n");

        // (B) 사용자 정보
        sb.append("### 사용자 정보\n")
                .append("- 목표 마라톤 거리: ").append(req.getGoalDist()).append("km\n")
                .append("- 목표 날짜: ").append(goalDate.format(fmt)).append("\n")
                .append("- 훈련 시작 날짜: ").append(startDate.format(fmt)).append("\n")
                .append("- 마라톤 경험: ").append(req.isRunExp() ? "있음" : "없음").append("\n")
                .append("- 최대 달리기 거리: ").append(req.getDistExp()).append("km\n")
                .append("- 주간 주행 빈도: ").append(req.getFreqExp()).append("회\n\n");

        // (C) 최근 기록 (이상치 제외)
        if (!histories.isEmpty()) {
            sb.append("### 최근 러닝 기록:\n");
            int idx = 1;
            for (History h : histories) {
                if (h.getDistance() < 0.1
                        || h.getAvgPace() < 2 || h.getAvgPace() > 15
                        || h.getAvgBpm() < 40 || h.getAvgBpm() > 220) continue;
                sb.append(idx++).append(". ")
                        .append("시작: ").append(h.getStartTime()).append(", ")
                        .append("종료: ").append(h.getEndTime()).append(", ")
                        .append("거리: ").append(h.getDistance()).append("km, ")
                        .append("케이던스: ").append(h.getAvgCadence()).append("spm, ")
                        .append("페이스: ").append(h.getAvgPace()).append("min/km, ")
                        .append("심박: ").append(h.getAvgBpm()).append("bpm\n");
            }
            sb.append("\n");
        }

        // (D) 이론적 근거: 에너지 시스템 및 지구력 유형
        sb.append("### 에너지 시스템 및 지구력 유형\n")
                .append("- STE(단거리): 35초–2분, 최대강도, HR 185–195bpm, VO₂max 100%, 젖산 10–18 mmol/L\n")
                .append("- MTE(중거리): 2–10분, 최대강도, HR 190–200bpm, VO₂max 95–100%, 젖산 12–20 mmol/L\n")
                .append("- LTE(장거리): 10분–6시간 이상, 중간~저강도, HR 60–80% HRmax, VO₂max 50–90%, 젖산 <5 mmol/L\n\n");

        // (E) 훈련 방식 및 페이스 가이드
        sb.append("### 훈련 방식 및 페이스 가이드라인\n")
                .append("- 회복주: Zone1 (60–70% HRmax), 7:30–8:30 min/km\n")
                .append("- 장거리 지구력주: Zone2 (70–85% HRmax), 목표페이스 +60–90초/km\n")
                .append("- 중간 속도 지속주: Zone3 (85–90% HRmax), 페이스 3:55–3:42 min/km\n")
                .append("- 템포주: 5–10 km, 레이스페이스 ±10%, 20–40분 유지\n")
                .append("- 인터벌: 확장형 8–15분 부하(60–70% 부하), 단기형 100–400 m 스퍼트(레이스페이스), 회복 조깅 400 m\n")
                .append("- 파틀렉: 1–2시간, 예열 + 스퍼트(50–200 m) + 조깅/언덕 반복 + 쿨다운\n\n");

        // (F) 주간 블록 구성
        sb.append("### 주간 훈련 블록 구성\n")
                .append("1. 중간 속도 지속주 1회  2. 장거리 지구력주 1회  3. 템포주 1회\n")
                .append("4. 인터벌 1회  5. 파틀렉 1회  6. 회복주 1–2회  7. 휴식일 1회\n\n");

        // (G) 거리 표기 양식
        sb.append("### 거리 표기 양식\n")
                .append("1. 숫자+단위(km) 예: 12km, 0.8km\n")
                .append("2. 인터벌 반복: 거리 x 횟수 예: 0.8km x 4회\n")
                .append("3. 구간별 표기(템포/회복): 쉼표 구분 km 예: 2km, 2km, 1km\n\n");

        // (H) 출력 JSON 스키마
        sb.append("### 출력 JSON 요구사항\n")
                .append("- 데이터 배열에는 훈련 시작 날짜(" + startDate.format(fmt) + ")부터 목표 날짜(" + goalDate.format(fmt) + ")까지 모든 날짜(휴식일 포함)가 누락 없이 포함되어야 합니다.\n")
                .append("```json\n")
                .append("{\n")
                .append("  \"data\": [\n")
                .append("    {\"date\": \"YYYY-MM-DD\", \"todo\": \"<훈련종류>: <거리><단위>(km 또는 m), <페이스/심박존>, 목적: <훈련 목적>\"},\n")
                .append("    ...\n")
                .append("  ]\n")
                .append("}\n")
                .append("```\n")
                .append("- 추가 설명/메타데이터 금지\n")
                .append("- 예시: \"장거리 지구력주: 15km, 6:30min/km, 목적: 유산소 지구력 향상\"\n");

        return sb.toString();
    }


    /**
     * Structured Outputs AI 호출
     */
    private String callOpenAiWithSchema(String prompt) {
        try {
            // 환경 변수 OPENAI_API_KEY 필요
            OpenAIClient client = OpenAIOkHttpClient.builder()
                    .fromEnv()
                    .apiKey(API_KEY)
                    .build();

            // 2) 스키마 정의: 최상위는 object, 그 안에 data 프로퍼티로 배열(items)
            // 배열(items) 안에 { date, todo } 객체
            // 각 객체는 date(string, 날짜 형식), todo(string) 필수
            Map<String, Object> itemProps = Map.of(
                    "date", Map.of("type", "string", "format", "date"),
                    "todo", Map.of("type", "string")
            );
            List<String> requiredFields = List.of("date", "todo");
            Map<String, Object> itemSchemaMap = Map.of(
                    "type", "object",
                    "properties", itemProps,
                    "required", requiredFields,
                    "additionalProperties", false
            );

            // 최상위 스키마: object with data:Array<item>
            Map<String, Object> rootProps = Map.of(
                    "data", Map.of(
                            "type", "array",
                            "items", itemSchemaMap
                    )
            );

            JsonSchema.Schema schema = JsonSchema.Schema.builder()
                    .putAdditionalProperty("type", JsonValue.from("object"))
                    .putAdditionalProperty("properties", JsonValue.from(rootProps))
                    .putAdditionalProperty("required", JsonValue.from(List.of("data")))
                    .putAdditionalProperty("additionalProperties", JsonValue.from(false))
                    .build();

            // 3) ChatCompletion 파라미터 구성
            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .model(ChatModel.GPT_4_1)
                    .maxCompletionTokens(8192)
                    .responseFormat(ResponseFormatJsonSchema.builder()
                            .jsonSchema(JsonSchema.builder()
                                    .name("curriculum-schedule")
                                    .schema(schema)
                                    .build())
                            .build())
                    .addUserMessage(prompt)
                    .build();

            // 4) AI 호출 및 JSON 문자열 수신
            var resp = client.chat()
                    .completions()
                    .create(params);

            // 5) 응답의 content 스트림을 이어붙여 반환
            return resp.choices().stream()
                    .flatMap(c -> c.message().content().stream())
                    .collect(Collectors.joining());
        } catch (ResponseStatusException e) {
            // 이미 HTTP 예외일 경우 그대로 던짐
            throw e;
        } catch (Exception e) {
            // 그 외 오류는 502로 변환
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "AI 호출 실패: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * AI가 준 JSON 배열을 파싱해 Map<날짜, ToDo>로 변환
     */
    private Map<LocalDate, String> parseAiResponse(String aiJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            // 1) 최상위 JSON 객체 파싱
            JsonNode root = mapper.readTree(aiJson);
            JsonNode dataNode = root.get("data");
            if (dataNode == null || !dataNode.isArray()) {
                throw new IllegalStateException("'data' 배열이 없습니다.");
            }
            // 2) data 배열을 List<Map<String,String>>로 변환
            List<Map<String, String>> list = mapper.convertValue(
                    dataNode,
                    new TypeReference<List<Map<String, String>>>() {
                    }
            );

            // 3) 날짜 문자 → LocalDate, todo 문자열 추출
            Map<LocalDate, String> result = new LinkedHashMap<>();
            for (Map<String, String> entry : list) {
                LocalDate date = LocalDate.parse(entry.get("date"));
                String todo = entry.get("todo");
                result.put(date, todo);
            }
            return result;
        } catch (Exception e) {
            // 파싱 오류 시 502로 변환
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "AI 응답 파싱 실패: " + e.getMessage(),
                    e
            );
        }
    }

    @Override
    public Todo getTodayTodo(UUID userId) {
        Todo todayTodo = curriculumDao.selectTodayTodoByUserId(userId);
        if (todayTodo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "오늘의 Todo가 없습니다.");
        }
        return todayTodo;
    }
}
