package com.runhwani.runmate.dto.response.history;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "내 상세 기록")
public class MyRunDetailResponse {
    @Schema(description = "달린 거리(km)", example = "5.0")
    private Double distance;
    
    @Schema(description = "달린 시간(초)", example = "1800")
    private long time;
    
    @Schema(description = "평균 페이스(초/km)", example = "360")
    private Double avgPace;
    
    @Schema(description = "평균 심박수(bpm)", example = "150")
    private Double avgBpm;
    
    @Schema(description = "소모 칼로리(kcal)", example = "300")
    private Double calories;
    
    @Schema(description = "코스에 추가 여부", example = "true")
    private boolean addedToCourse;
} 