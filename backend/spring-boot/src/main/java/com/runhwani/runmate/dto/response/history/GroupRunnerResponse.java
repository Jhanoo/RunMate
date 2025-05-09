package com.runhwani.runmate.dto.response.history;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "그룹 러닝 참여자 기록")
public class GroupRunnerResponse {
    @Schema(description = "사용자 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID userId;
    
    @Schema(description = "닉네임", example = "alice")
    private String nickname;
    
    @Schema(description = "달린 거리(km)", example = "5.0")
    private Double distance;
    
    @Schema(description = "달린 시간(초)", example = "1800")
    private long time;
    
    @Schema(description = "평균 페이스(초/km)", example = "360")
    private Double avgPace;
} 