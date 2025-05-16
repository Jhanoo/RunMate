package com.runhwani.runmate.dto.response.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;
import java.time.LocalDate;
import com.runhwani.runmate.model.Gender;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원가입 응답")
public class SignupResponse {
    @Schema(description = "사용자 ID")
    private UUID userId;
    
    @Schema(description = "이메일")
    private String email;
    
    @Schema(description = "닉네임")
    private String nickname;
    
    @Schema(description = "프로필 이미지 URL")
    private String profileImage;
    
    @Schema(description = "생일")
    private LocalDate birthday;
    
    @Schema(description = "성별")
    private Gender gender;
    
    @Schema(description = "키 (cm)")
    private Double height;
    
    @Schema(description = "몸무게 (kg)")
    private Double weight;
    
    @Schema(description = "FCM 토큰")
    private String fcmToken;
} 