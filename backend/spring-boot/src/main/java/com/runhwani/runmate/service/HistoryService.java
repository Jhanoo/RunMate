package com.runhwani.runmate.service;

import com.runhwani.runmate.dto.response.history.HistoryListResponse;

public interface HistoryService {
    /**
     * 사용자의 달리기 기록 목록을 페이징하여 조회
     * @param page 페이지 번호 (1부터 시작)
     * @param size 페이지 크기
     * @return 페이징된 달리기 기록 목록
     */
    HistoryListResponse getHistoryList(int page, int size);
} 