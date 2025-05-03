package com.runhwani.runmate.model;

import lombok.*;
import java.util.UUID;
import java.time.LocalDateTime;

/**
 * 코스 좋아요 기록
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseLike {
    /** PK (course_likes.like_id) */
    private UUID likeId;
    /** 사용자 (users.user_id FK) */
    private UUID userId;
    /** 코스 (courses.course_id FK) */
    private UUID courseId;
    /** 좋아요 시각 */
    private LocalDateTime likedAt;
}
