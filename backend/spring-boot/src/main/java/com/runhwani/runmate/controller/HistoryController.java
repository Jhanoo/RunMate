package com.runhwani.runmate.controller;

import com.runhwani.runmate.controller.docs.HistoryControllerDocs;
import com.runhwani.runmate.dto.common.CommonResponse;
import com.runhwani.runmate.dto.response.history.HistoryDetailResponse;
import com.runhwani.runmate.dto.response.history.HistoryListResponse;
import com.runhwani.runmate.service.HistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/histories")
@RequiredArgsConstructor
@Slf4j
public class HistoryController implements HistoryControllerDocs {

    private final HistoryService historyService;

    @Override
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<CommonResponse<HistoryListResponse>> getHistoryList(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        
        // 서비스 호출
        HistoryListResponse response = historyService.getHistoryList(page, size);
        
        return ResponseEntity.ok(new CommonResponse<>("히스토리 목록 조회 성공", response));
    }
    
    @Override
    @RequestMapping(value = "/{historyId}", method = RequestMethod.GET)
    public ResponseEntity<CommonResponse<HistoryDetailResponse>> getHistoryDetail(
            @PathVariable(value = "historyId") UUID historyId) {
        
        // 서비스 호출
        HistoryDetailResponse response = historyService.getHistoryDetail(historyId);
        
        return ResponseEntity.ok(new CommonResponse<>("히스토리 상세 조회 성공", response));
    }
} 