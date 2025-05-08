package com.runhwani.runmate.dto.response.user;

import com.runhwani.runmate.model.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 프로필 응답")
public class UserProfileResponse {
    @Schema(description = "이메일", example = "user@example.com")
    private String email;
    
    @Schema(description = "닉네임", example = "러너1")
    private String nickname;
    
    @Schema(description = "생일", example = "1990-01-01")
    private LocalDate birthday;
    
    @Schema(description = "성별", example = "MALE")
    private Gender gender;
    
    @Schema(description = "프로필 이미지 URL", example = "https://example.com/images/profile.jpg")
    private String profileImageUrl;
} 