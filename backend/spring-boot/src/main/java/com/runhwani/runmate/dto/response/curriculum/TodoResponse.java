package com.runhwani.runmate.dto.response.curriculum;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Todo 응답 정보")
public class TodoResponse {

    @Schema(description = "Todo ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID todoId;

    @Schema(description = "할 일 내용", example = "5km 속도 연습")
    private String content;

    @Schema(description = "완료 여부", example = "false")
    private Boolean isDone;

    @Schema(description = "할 일 일시 (ISO 8601)", example = "2025-05-10T00:00:00+09:00")
    private OffsetDateTime date;
}