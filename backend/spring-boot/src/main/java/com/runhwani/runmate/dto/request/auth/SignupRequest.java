package com.runhwani.runmate.dto.request.auth;

import com.runhwani.runmate.model.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import org.springframework.web.multipart.MultipartFile;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원가입 요청")
public class SignupRequest {
    @Schema(description = "이메일", example = "user@example.com", required = true)
    private String email;
    
    @Schema(description = "비밀번호", example = "Password123!", required = true)
    private String password;
    
    @Schema(description = "닉네임", example = "러너1", required = true)
    private String nickname;
    
    @Schema(description = "생일", example = "1990-01-01")
    private String birthdayStr;
    
    @Schema(description = "성별", example = "MALE")
    private String genderStr;
    
    @Schema(description = "프로필 이미지", type = "string", format = "binary")
    private MultipartFile profileImage;
    
    public LocalDate getBirthday() {
        return birthdayStr != null && !birthdayStr.isEmpty() ? LocalDate.parse(birthdayStr) : null;
    }
    
    public Gender getGender() {
        return genderStr != null && !genderStr.isEmpty() ? Gender.valueOf(genderStr) : null;
    }
} 