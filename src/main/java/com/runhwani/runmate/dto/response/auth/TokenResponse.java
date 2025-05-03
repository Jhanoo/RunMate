package com.runhwani.runmate.dto.response.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "토큰 응답")
public class TokenResponse {
    
    @Schema(description = "액세스 토큰")
    private String accessToken;
} 