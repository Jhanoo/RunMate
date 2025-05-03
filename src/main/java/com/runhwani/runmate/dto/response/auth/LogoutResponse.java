package com.runhwani.runmate.dto.response.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "로그아웃 응답")
public class LogoutResponse {
    @Schema(description = "응답 메시지")
    private String message;
} 