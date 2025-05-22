package com.runhwani.runmate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 커리큘럼 내 할 일 항목
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Todo {
    /** PK (todos.todo_id) */
    private UUID todoId;
    /** 커리큘럼 (curricula.curriculum_id FK) */
    private UUID curriculumId;
    /** 유저 ID */
    private UUID userId;
    /** 할 일 내용 */
    private String content;
    /** 완료 여부 */
    private Boolean isDone;
    /** 수행 날짜 */
    private OffsetDateTime date;
    
    // isDone이 null인 경우 false를 반환하는 안전한 getter
    public boolean isDoneOrFalse() {
        return isDone != null && isDone;
    }
}
