package com.runhwani.runmate.dto.response.history;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "히스토리 참여자 상세 기록 응답")
public class RunnerDetailResponse {
    
    @Schema(description = "사용자 ID")
    private UUID userId;
    
    @Schema(description = "닉네임")
    private String nickname;
    
    @Schema(description = "프로필 이미지 URL")
    private String profileImage;
    
    @Schema(description = "시작 시간")
    private OffsetDateTime startTime;
    
    @Schema(description = "종료 시간")
    private OffsetDateTime endTime;
    
    @Schema(description = "달린 거리(km)")
    private Double distance;
    
    @Schema(description = "GPX 파일 URL")
    private String gpxFile;
    
    @Schema(description = "소모 칼로리")
    private Double calories;
    
    @Schema(description = "평균 케이던스")
    private Double avgCadence;
    
    @Schema(description = "평균 페이스(초/km)")
    private Double avgPace;
    
    @Schema(description = "평균 심박수")
    private Double avgBpm;
    
    @Schema(description = "평균 고도")
    private Double avgElevation;
} 