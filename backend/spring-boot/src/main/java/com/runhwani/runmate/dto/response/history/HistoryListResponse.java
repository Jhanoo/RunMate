package com.runhwani.runmate.dto.response.history;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "달리기 기록 목록 응답")
public class HistoryListResponse {
    @Schema(description = "전체 기록 수", example = "50")
    private long total;
    
    @Schema(description = "현재 페이지", example = "1")
    private int page;
    
    @Schema(description = "페이지 크기", example = "10")
    private int size;
    
    @Schema(description = "기록 목록")
    private List<HistoryResponse> histories;
} 