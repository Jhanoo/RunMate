package com.runhwani.runmate.global.docs;

import com.runhwani.runmate.domain.auth.dto.LoginRequest;
import com.runhwani.runmate.domain.auth.dto.SignupRequest;
import com.runhwani.runmate.domain.auth.dto.TokenResponse;
import com.runhwani.runmate.domain.auth.dto.LogoutResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "1. 인증", description = "인증 관련 API")
public interface AuthControllerDocs {

    @Operation(
        summary = "회원가입",
        description = "새로운 사용자를 등록합니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SignupRequest.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "email": "user@example.com",
                      "password": "password123",
                      "nickname": "홍길동"
                    }
                    """
                )
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "회원가입 성공",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TokenResponse.class),
                    examples = @ExampleObject(
                        value = """
                        {
                          "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                        }
                        """
                    )
                )
            )
        }
    )
    ResponseEntity<TokenResponse> signup(SignupRequest request);
    
    @Operation(
        summary = "로그인",
        description = "사용자 로그인을 처리합니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginRequest.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "email": "user@example.com",
                      "password": "password123"
                    }
                    """
                )
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "로그인 성공",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TokenResponse.class)
                )
            )
        }
    )
    ResponseEntity<TokenResponse> login(LoginRequest request);
    
    @Operation(
        summary = "로그아웃",
        description = "사용자 로그아웃을 처리하고 토큰을 무효화합니다.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "로그아웃 성공",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LogoutResponse.class),
                    examples = @ExampleObject(
                        value = """
                        {
                          "message": "로그아웃 되었습니다."
                        }
                        """
                    )
                )
            )
        }
    )
    ResponseEntity<LogoutResponse> logout(String bearerToken);
} 