package com.runhwani.runmate.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 사용자별 마라톤 커리큘럼
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자별 마라톤 커리큘럼")
public class Curriculum {
    @Schema(description = "커리큘럼 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID curriculumId;

    @Schema(description = "사용자 ID", example = "e7a1f64c-1234-4bcd-9a0d-ffc2d9e5c123")
    private UUID userId;

    @Schema(description = "마라톤 ID (nullable)", example = "2d1b6e35-5678-4f29-8b6d-1a2f3c4d5e6f")
    private UUID marathonId;

    @Schema(description = "목표 거리", example = "10km")
    private String goalDist;

    @Schema(description = "목표 날짜 (ISO 8601 형식)", example = "2025-06-10T08:00:00+09:00")
    private OffsetDateTime goalDate;

    @Schema(description = "달리기 경험 여부", example = "true")
    private Boolean runExp;

    @Schema(description = "경험 거리(예: '~10km')", example = "~10km")
    private String distExp;

    @Schema(description = "주당 빈도(예: '1~2회')", example = "3~4회")
    private String freqExp;

    @Schema(description = "커리큘럼 종료 여부", example = "false")
    private Boolean isFinished;
}

