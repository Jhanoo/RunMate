package com.runhwani.runmate.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "로그인 요청")
public class LoginRequest {
    @Schema(description = "이메일")
    private String email;
    
    @Schema(description = "비밀번호")
    private String password;
    
    @Schema(description = "FCM 토큰")
    private String fcmToken;
} 