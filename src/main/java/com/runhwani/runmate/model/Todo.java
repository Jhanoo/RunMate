package com.runhwani.runmate.model;

import lombok.*;
import java.util.UUID;
import java.time.LocalDateTime;

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
    /** 할 일 내용 */
    private String content;
    /** 완료 여부 */
    private Boolean isDone;
    /** 수행 날짜 */
    private LocalDateTime date;
}
