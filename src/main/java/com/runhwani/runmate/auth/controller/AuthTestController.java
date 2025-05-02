package com.runhwani.runmate.auth.controller;

import com.runhwani.runmate.global.jwt.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증", description = "인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthTestController {

    private final JwtProvider jwtProvider;

    @Operation(
        summary = "테스트 토큰 발급",
        description = "테스트용 JWT 토큰을 발급합니다.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "토큰 발급 성공",
                content = @Content(schema = @Schema(
                    example = "{\"accessToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"}"
                ))
            )
        }
    )
    @PostMapping("/test-token")
    public ResponseEntity<TokenResponse> getTestToken() {
        String token = jwtProvider.generateToken("test@test.com");
        return ResponseEntity.ok(new TokenResponse(token));
    }

    @Operation(summary = "인증 테스트", description = "JWT 토큰 인증을 테스트합니다.")
    @GetMapping("/test-auth")
    public ResponseEntity<String> testAuth() {
        return ResponseEntity.ok("인증 성공!");
    }
}

record TokenResponse(String accessToken) {} 