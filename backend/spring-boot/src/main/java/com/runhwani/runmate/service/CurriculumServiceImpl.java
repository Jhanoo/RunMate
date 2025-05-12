package com.runhwani.runmate.service;

import com.fasterxml.jackson.core.type.TypeReference;
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
import com.runhwani.runmate.model.Todo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
        var histories = historyDao.findByUserIdSinceDate(userId, since);

        log.debug("히스토리 = {}", histories);

        // 2) 프롬프트 생성
        String prompt = buildPrompt(histories, req);

        // 3) AI 호출 (스키마 적용)
        String aiJson = callOpenAiWithSchema(prompt, req.getGoalDate());

        log.debug("========\nAI 대답: {}", aiJson);

        // 4) JSON 파싱 → Map<LocalDate, List<String>>
        Map<LocalDate, List<String>> schedule = parseAiResponse(aiJson);

        // 5) Curriculum 엔티티 저장
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

        // 6) Todo 엔티티들 저장
        schedule.forEach((date, todos) -> {
            for (String content : todos) {
                Todo todo = Todo.builder()
                        .todoId(UUID.randomUUID())
                        .curriculumId(curriculumId)
                        .userId(userId)
                        .content(content)
                        .date(date.atStartOfDay(ZoneId.of("Asia/Seoul"))
                                .toOffsetDateTime())
                        .build();
                curriculumDao.insertTodo(todo);
            }
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
        Curriculum curriculum = curriculumDao.selectByUserId(userId);
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

        return curriculumDao.selectByPeriod(userId, start, end);
    }

    /**
     * 프롬프트 작성부
     */
    private String buildPrompt(List<?> histories, CurriculumCreateRequest req) {
        // 1. 서울 시간대로 오늘과 시작 날짜 계산
        ZoneId seoul = ZoneId.of("Asia/Seoul");
        LocalDate todaySeoul = LocalDate.now(seoul);
        // 다음 날부터 훈련을 시작한다고 가정
        LocalDate startDate = todaySeoul.plusDays(1);
        LocalDate goalDate = req.getGoalDate().toLocalDate();
        // 날짜 포맷터 (예: "2025-06-01")
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;

        var sb = new StringBuilder();
        // (A) 프롬프트 역할 설정
        sb.append("당신은 마라톤 전문가입니다.\n");
        // (B) 최근 1달 기록을 나열
        sb.append("사용자의 최근 한 달 달리기 기록은 다음과 같습니다:\n");
        for (var h : histories) {
            sb.append("  - ").append(h).append("\n");
        }
        // (C) 목표 정보와 훈련 기간 명시
        sb.append("목표 달리기 거리: ").append(req.getGoalDist()).append("km\n");
        sb.append("목표 날짜: ").append(goalDate).append("\n");
        sb.append("훈련 시작 날짜: ").append(startDate).append("\n");
        sb.append("마라톤 경험 유무: ").append(req.isRunExp() ? "있음" : "없음").append("\n");
        sb.append("현재 달릴 수 있는 최대 거리: ").append(req.getDistExp()).append("km\n");
        sb.append("주간 달리기 빈도: ").append(req.getFreqExp()).append("회\n\n");
        // (D) 출력 형식 지시
        sb.append("아래 예시처럼, 시작 날짜(")
                .append(startDate.format(fmt))
                .append(")부터 목표 날짜(")
                .append(goalDate.format(fmt))
                .append(")까지 날짜별로 “날짜 → ToDo 리스트” 형태의 JSON만 출력해 주세요.\n")
                .append("각 ToDo 항목은 전문가가 조언하듯 완전한 문장으로 작성해 주세요.\n")
                .append("예시:\n")
                .append("{\n")
                .append("  \"").append(startDate.format(fmt)).append("\": [\n")
                .append("    \"6월 1일에는 가벼운 조깅으로 몸을 풀고, 충분한 스트레칭을 실시하세요.\",\n")
                .append("    \"근력 강화를 위해 스쿼트와 런지 각 15회를 수행하세요.\"\n")
                .append("  ],\n")
                .append("  \"").append(startDate.plusDays(1).format(fmt)).append("\": [\n")
                .append("    \"휴식일로 지정하고, 가벼운 요가로 몸의 피로를 풀어주세요.\"\n")
                .append("  ],\n")
                .append("  …\n")
                .append("}\n");

        return sb.toString();
    }


    /**
     * Structured Outputs AI 호출
     */
    private String callOpenAiWithSchema(String prompt, OffsetDateTime goalDate) {
        try {
            // 환경 변수 OPENAI_API_KEY 필요
            OpenAIClient client = OpenAIOkHttpClient.builder()
                    .fromEnv()
                    .apiKey(API_KEY)
                    .build();


            // 2) 날짜 리스트 생성 (내일부터 목표일까지)
            ZoneId zone = ZoneId.of("Asia/Seoul");
            LocalDate today = LocalDate.now(zone);
            LocalDate endDate = goalDate.toLocalDate();

            Map<String, Object> props = new LinkedHashMap<>();
            for (LocalDate d = today.plusDays(1); !d.isAfter(endDate); d = d.plusDays(1)) {
                // 각 날짜별 스키마: string 배열
                Map<String, Object> arrSchema = Map.of(
                        "type", "array",
                        "items", Map.of("type", "string")
                );
                props.put(d.toString(), arrSchema);
            }

            // 날짜→배열 형태 JSON 스키마 정의
            JsonSchema.Schema schema = JsonSchema.Schema.builder()
                    .putAdditionalProperty("type", JsonValue.from("object"))
                    .putAdditionalProperty("properties", JsonValue.from(props))
                    .putAdditionalProperty("required", JsonValue.from(new ArrayList<>(props.keySet())))
                    .putAdditionalProperty("additionalProperties", JsonValue.from(false))
                    .build();

            // ChatCompletion 파라미터
            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .model(ChatModel.GPT_4_1)
                    .maxCompletionTokens(2048)
                    .responseFormat(ResponseFormatJsonSchema.builder()
                            .jsonSchema(JsonSchema.builder()
                                    .name("curriculum-schedule")
                                    .schema(schema)
                                    .build())
                            .build())
                    .addUserMessage(prompt)
                    .build();

            // API 호출 및 JSON 문자열 반환
            var resp = client.chat()
                    .completions()
                    .create(params);

            return resp.choices().stream()
                    .flatMap(c -> c.message().content().stream())
                    .collect(Collectors.joining());
        } catch (ResponseStatusException e) {
            // 이미 ResponseStatusException이면 그대로 던짐
            throw e;
        } catch (Exception e) {
            // 호출 실패 시 502 Bad Gateway
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "AI 호출 실패: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * AI가 준 JSON을 Map<날짜, ToDo리스트>로 파싱
     */
    private Map<LocalDate, List<String>> parseAiResponse(String aiJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            var tmp = mapper.readValue(
                    aiJson,
                    new TypeReference<Map<String, List<String>>>() {
                    }
            );

            // 순서를 보장하려면 LinkedHashMap 사용
            Map<LocalDate, List<String>> result = new LinkedHashMap<>();
            tmp.forEach((k, v) -> result.put(LocalDate.parse(k), v));
            return result;
        } catch (Exception e) {
            // 파싱 오류 시 502 Bad Gateway
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "AI 응답 파싱 실패: " + e.getMessage(),
                    e
            );
        }
    }
}
