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


    /**
     * 특정 연·월의 Todo 리스트 조회
     * @param userId 사용자 ID
     * @param year 조회 연도
     * @param month 조회 월 (1~12)
     * @return 해당 월의 Todo 리스트
     */
    List<Todo> getTodoListByMonth(UUID userId, int year, int month);
}
