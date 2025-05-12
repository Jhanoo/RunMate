package com.runhwani.runmate.dto.request.curriculum;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "커리큘럼 생성 요청")
public class CurriculumCreateRequest {
    @Schema(description = "마라톤 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID marathonId;
    @Schema(description = "목표 거리 (km)", example = "42.195")
    private String goalDist;
    @Schema(description = "목표 날짜 (ISO 8601)", example = "2025-06-10T09:00:00+09:00")
    private OffsetDateTime goalDate;
    @Schema(description = "마라톤 경험 유무", example = "true")
    private boolean runExp;
    @Schema(description = "현재 달릴 수 있는 최대 거리 (km)", example = "10")
    private String distExp;
    @Schema(description = "주간 달리기 빈도 (회)", example = "3")
    private String freqExp;
}

