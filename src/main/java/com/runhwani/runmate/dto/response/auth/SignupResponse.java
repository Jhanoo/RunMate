package com.runhwani.runmate.dto.response.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

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
} 