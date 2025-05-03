package com.runhwani.runmate.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "회원가입 요청")
public class SignupRequest {
    @Schema(description = "이메일")
    private String email;
    
    @Schema(description = "비밀번호")
    private String password;
    
    @Schema(description = "닉네임")
    private String nickname;
} 