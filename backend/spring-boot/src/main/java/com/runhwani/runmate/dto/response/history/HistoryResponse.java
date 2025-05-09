package com.runhwani.runmate.dto.response.history;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "달리기 기록 응답")
public class HistoryResponse {
    @Schema(description = "기록 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID historyId;
    
    @Schema(description = "코스 이름", example = "한강 러닝")
    private String courseName;
    
    @Schema(description = "그룹 이름", example = "월요 모임")
    private String groupName;
    
    @Schema(description = "시작 위치", example = "반포 한강공원")
    private String location;
    
    @Schema(description = "시작 시각", example = "2025-05-10T07:00:00Z")
    private OffsetDateTime startTime;
    
    @Schema(description = "달린 시간(초)", example = "3600")
    private long duration;
    
    @Schema(description = "그룹 멤버 프로필 이미지 URL 목록")
    private List<String> members;
    
    @Schema(description = "내가 달린 거리(km)", example = "10.3")
    private Double myDistance;
} 