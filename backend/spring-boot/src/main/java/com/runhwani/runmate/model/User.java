package com.runhwani.runmate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

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
    /** 생일 */
    private LocalDate birthday;
    /** 성별 */
    private Gender gender;
    /** FCM 토큰 */
    private String fcmToken;
    /** 키 (cm) */
    private Double height;
    /** 몸무게 (kg) */
    private Double weight;
    /** 생성 시각 */
    private OffsetDateTime createdAt;
}
