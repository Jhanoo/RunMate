package com.runhwani.runmate.dto.request.auth;

import com.runhwani.runmate.model.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.annotation.JsonIgnore;

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
    private LocalDate birthday;
    
    @Schema(description = "성별", example = "MALE")
    private Gender gender;
    
    @Schema(hidden = true)
    private MultipartFile profileImage;
    

} 