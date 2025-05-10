package com.runhwani.runmate.dao;

import com.runhwani.runmate.model.Curriculum;
import com.runhwani.runmate.model.Todo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CurriculumDao {

    /**
     * curriculum 테이블에 새 레코드 삽입
     * @param curriculum 삽입할 엔티티
     */
    void insertCurriculum(Curriculum curriculum);


    /**
     * curriculum 테이블에 새 레코드 삽입
     * @param todo 삽입할 엔티티
     */
    void insertTodo(Todo todo);
}
