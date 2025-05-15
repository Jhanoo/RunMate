package com.runhwani.runmate.dto.response.history;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "달리기 기록 상세 응답")
public class HistoryDetailResponse {
    @Schema(description = "기록 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID historyId;
    
    @Schema(description = "GPX 파일 경로", example = "/gpx/abc123.gpx")
    private String gpxFile;
    
    @Schema(description = "그룹 러닝 참여자 기록 목록")
    private List<GroupRunnerResponse> groupRun;
    
    @Schema(description = "내 상세 기록")
    private MyRunDetailResponse myRun;
}