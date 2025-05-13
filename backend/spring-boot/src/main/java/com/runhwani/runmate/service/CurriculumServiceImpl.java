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
            Boolean isDoneVal = content.matches("^\\d+km.*") ? false : null;

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
        LocalDate todaySeoul = LocalDate.now(seoul);
        // 다음 날부터 훈련을 시작한다고 가정
        LocalDate startDate = todaySeoul.plusDays(1);
        LocalDate goalDate = req.getGoalDate().toLocalDate();
        // 날짜 포맷터 (예: "2025-06-01")
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;

        StringBuilder sb = new StringBuilder();
        // (A) 프롬프트 역할 설정
        sb.append("당신은 전문 마라톤 트레이너입니다.\n");
        sb.append("다음 정보를 바탕으로, 훈련 시작일부터 목표일까지 하루 단위 JSON 배열을 생성해주세요.\n\n");

        // (B) 목표 정보
        sb.append("목표 마라톤 거리: ").append(req.getGoalDist()).append("\n")
                .append("목표 날짜: ").append(goalDate.format(fmt)).append("\n")
                .append("훈련 시작 날짜: ").append(startDate.format(fmt)).append("\n")
                .append("마라톤 경험 유무: ").append(req.isRunExp() ? "있음" : "없음").append("\n")
                .append("현재 달릴 수 있는 최대 거리: ").append(req.getDistExp()).append("\n")
                .append("일주일 달리기 빈도: ").append(req.getFreqExp()).append("\n\n");

        // (C) 최근 1달 기록
        if (!histories.isEmpty()) {
            sb.append("사용자의 최근 러닝 기록:\n");
            int idx = 1;
            for (History h : histories) {
                sb.append(idx++).append(". ")
                        .append("시작: ").append(h.getStartTime()).append(", ")
                        .append("종료: ").append(h.getEndTime()).append(", ")
                        .append("거리: ").append(h.getDistance()).append("km, ")
                        .append("평균 케이던스: ").append(h.getAvgCadence()).append("spm, ")
                        .append("평균 페이스: ").append(h.getAvgPace()).append("min/km, ")
                        .append("평균 심박수: ").append(h.getAvgBpm()).append("bpm\n");
            }
            sb.append("\n");
        }

        // (D) 출력 형식 지시
        sb.append("요구사항:\n")
                .append("1. ").append(startDate.format(fmt))
                .append("부터 ").append(goalDate.format(fmt))
                .append("까지 날짜별로 JSON 객체를 하루 하나씩 생성해주세요.\n")
                .append("2. 달리기 거리 조언(예: 3km 달리기, 5km 달리기)을 제시할 때, " +
                        "반드시 문장의 맨 앞이 ‘3km’, ‘5km’처럼 '<거리>km' 형태로 시작하도록 해주세요.\\n")
                .append("3. 출력은 반드시 다음과 같은 JSON 객체 형태로만 보여주세요:\n")
                .append("""
                        ```json
                        {
                          "data": [
                            { "date": "YYYY-MM-DD", "todo": "구체적 훈련 조언 문장" },
                            …
                          ]
                        }
                        ```
                        """)
                .append("4. 추가 설명이나 메타데이터는 절대 포함하지 마시고, 위 형식만 지켜주세요.");

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
                    .model(ChatModel.O4_MINI)
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
}
