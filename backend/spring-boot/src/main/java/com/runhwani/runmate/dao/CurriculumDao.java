package com.runhwani.runmate.dao;

import com.runhwani.runmate.model.Curriculum;
import com.runhwani.runmate.model.Todo;
import org.apache.ibatis.annotations.Mapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Mapper
public interface CurriculumDao {

    /**
     * curriculum 테이블에 새 레코드 삽입
     *
     * @param curriculum 삽입할 엔티티
     */
    void insertCurriculum(Curriculum curriculum);


    /**
     * curriculum 테이블에 새 레코드 삽입
     *
     * @param todo 삽입할 엔티티
     */
    void insertTodo(Todo todo);

    /**
     * curriculum 조회
     *
     * @param userId 조회할 유저 ID
     */
    Curriculum selectCurriculumByUserId(UUID userId);

    /**
     * 주어진 기간(period) 내의 Todo 조회
     *
     * @param userId User ID
     * @param start  조회 시작 시각 (inclusive)
     * @param end    조회 종료 시각 (exclusive)
     */
    List<Todo> selectTodoListByPeriod(UUID userId, OffsetDateTime start, OffsetDateTime end);

    /**
     * 커리큘럼의 is_finished를 true로 갱신
     *
     * @param curriculumId 수정할 커리큘럼 ID
     */
    void updateCurriculumIsFinished(UUID curriculumId);


    /**
     * 매일 자정마다 커리큘럼 목표 날짜가 지나면 커리큘럼의 is_finished=false를 true로 갱신
     */
    void updateCurriculumIsFinishedEveryDay();

    /**
     * 커리큘럼이 바뀌면서 앞으로의 Todo들 삭제
     *
     * @param userId 유저 ID
     */
    void deleteTodoList(UUID userId);

    /**
     * Todo의 isDone을 true로 갱신
     *
     * @param userId 유저 ID
     */
    void updateTodoDone(UUID userId);
}
