package com.runhwani.runmate.service;

import com.runhwani.runmate.dto.request.curriculum.CurriculumCreateRequest;
import com.runhwani.runmate.model.Curriculum;
import com.runhwani.runmate.model.Todo;

import java.util.List;
import java.util.UUID;

public interface CurriculumService {

    /**
     * 새 커리큘럼 생성.
     *
     * @param userId  요청자 ID
     * @param request goalDist, goalDate, runExp, distExp, freqExp 포함
     * @return 생성된 curriculumId
     */
    UUID generateCurriculum(UUID userId, CurriculumCreateRequest request);


    /**
     * 나의 커리큘럼 조회
     *
     * @param userId 요청자 ID
     */
    Curriculum getMyCurriculum(UUID userId);

}
