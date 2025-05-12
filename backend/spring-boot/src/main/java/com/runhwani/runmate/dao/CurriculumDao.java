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
    Curriculum selectByUserId(UUID userId);

    /**
     * 주어진 기간(period) 내의 Todo 조회
     *
     * @param userId User ID
     * @param start  조회 시작 시각 (inclusive)
     * @param end    조회 종료 시각 (exclusive)
     */
    List<Todo> selectByPeriod(UUID userId, OffsetDateTime start, OffsetDateTime end);
}
