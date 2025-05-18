package com.runhwani.runmate.dao;

import com.runhwani.runmate.model.Todo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Mapper
public interface NotificationTodoDao {
    /**
     * 할 일 ID로 조회
     */
    Todo findById(UUID todoId);
    
    /**
     * 사용자 ID와 날짜로 할 일 목록 조회
     */
    List<Todo> findTodosByUserIdAndDate(@Param("userId") UUID userId, @Param("date") LocalDate date);
    
    /**
     * 특정 날짜의 모든 할 일 조회 (모든 사용자)
     */
    List<Todo> findAllTodosByDate(LocalDate date);
    
    /**
     * 할 일 추가
     */
    void insert(Todo todo);
    
    /**
     * 할 일 완료 상태 업데이트
     */
    void updateStatus(@Param("todoId") UUID todoId, @Param("isDone") boolean isDone);
    
    /**
     * 할 일 삭제
     */
    void delete(UUID todoId);
} 