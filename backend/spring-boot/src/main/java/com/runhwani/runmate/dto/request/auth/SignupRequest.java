package com.runhwani.runmate.dto.request.auth;

import com.runhwani.runmate.model.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@Schema(description = "회원가입 요청")
public class SignupRequest {
    @Schema(description = "이메일", required = true)
    private String email;
    
    @Schema(description = "비밀번호", required = true)
    private String password;
    
    @Schema(description = "닉네임", required = true)
    private String nickname;
    
    @Schema(description = "생일", example = "1990-01-01")
    private LocalDate birthday;
    
    @Schema(description = "성별", example = "MALE")
    private Gender gender;
} 