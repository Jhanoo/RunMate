package com.runhwani.runmate.service;

import com.runhwani.runmate.dto.request.run.RunEndRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 러닝 기록 저장을 담당하는 서비스 인터페이스
 */
public interface RunService {
    /**
     * GPX 파일과 통계 정보를 받아 달리기 기록을 저장한다.
     * 저장된 히스토리아이디도 반환해버리겟다.
     *
     * @param gpxFile GPX 경로 파일
     * @param request 달리기 종료 통계 정보
     */
    UUID endRun(UUID userId, MultipartFile gpxFile, RunEndRequest request) throws IOException;
}
