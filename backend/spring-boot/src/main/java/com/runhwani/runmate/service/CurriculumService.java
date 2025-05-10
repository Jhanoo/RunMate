package com.runhwani.runmate.service;

import com.runhwani.runmate.dto.request.curriculum.CurriculumCreateRequest;

import java.util.UUID;

public interface CurriculumService {

    /**
     * 새 커리큘럼 생성.
     * @param userId   요청자 ID
     * @param request  goalDist, goalDate, runExp, distExp, freqExp 포함
     * @return 생성된 curriculumId
     */
    UUID generateCurriculum(UUID userId, CurriculumCreateRequest request);
}
