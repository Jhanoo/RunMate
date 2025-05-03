package com.runhwani.runmate.model;

import lombok.*;
import java.util.UUID;
import java.time.LocalDateTime;

/**
 * 사용자 정보
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    /** PK (users.user_id) */
    private UUID userId;
    /** 이메일 */
    private String email;
    /** 비밀번호 */
    private String password;
    /** 닉네임 */
    private String nickname;
    /** 프로필 이미지 URL */
    private String profileImage;
    /** 평균 페이스 */
    private Double avgPace;
    /** 생성 시각 */
    private LocalDateTime createdAt;
}
