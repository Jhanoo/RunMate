package com.runhwani.runmate.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "로그인 요청")
public class LoginRequest {
    @Schema(description = "이메일")
    private String email;
    
    @Schema(description = "비밀번호")
    private String password;
} 