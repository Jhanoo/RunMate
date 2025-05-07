package com.runhwani.runmate.dto.request.run;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "달리기 종료 요청 정보")
public class RunEndRequest {
    @Schema(description = "출발 위치", example = "여의도 한강공원")
    private String startLocation;

    @Schema(description = "시작 시각 (ISO-8601)", example = "2025-05-10T08:00:00+09:00")
    private OffsetDateTime startTime;

    @Schema(description = "종료 시각 (ISO-8601)", example = "2025-05-10T09:15:30+09:00")
    private OffsetDateTime endTime;

    @Schema(description = "달린 거리 (km)", example = "10.3")
    private Double distance;

    @Schema(description = "평균 심박수 (bpm)", example = "145.2")
    private Double avgBpm;

    @Schema(description = "평균 페이스 (분/㎞)", example = "5.23")
    private Double avgPace;

    @Schema(description = "평균 케이던스 (spm)", example = "160.5")
    private Double avgCadence;

    @Schema(description = "평균 고도 (m)", example = "12.3")
    private Double avgElevation;

    @Schema(description = "소모 칼로리 (kcal)", example = "575.4")
    private Double calories;
}
