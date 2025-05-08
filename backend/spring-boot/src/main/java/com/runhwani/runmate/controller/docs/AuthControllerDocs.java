package com.runhwani.runmate.controller.docs;

import com.runhwani.runmate.dto.common.CommonResponse;
import com.runhwani.runmate.dto.request.auth.LoginRequest;
import com.runhwani.runmate.dto.request.auth.SignupRequest;
import com.runhwani.runmate.dto.response.auth.SignupResponse;
import com.runhwani.runmate.dto.response.auth.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "인증", description = "인증 관련 API")
@RequestMapping("/api/auth")
public interface AuthControllerDocs {

    @Operation(
            summary = "회원가입",
            description = "새로운 사용자를 등록합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SignupRequest.class),
                            examples = @ExampleObject(
                                    name = "회원가입 요청 예시",
                                    value = """
                                    {
                                        "email": "user@example.com",
                                        "password": "Password123!",
                                        "nickname": "runner1",
                                        "birthday": "1990-01-01",
                                        "gender": "MALE"
                                    }
                                    """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "회원가입 성공",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "회원가입 성공 응답 예시",
                                            value = """
                                            {
                                                "message": "회원가입 성공",
                                                "data": {
                                                    "userId": "123e4567-e89b-12d3-a456-426614174000",
                                                    "email": "user@example.com",
                                                    "nickname": "runner1",
                                                    "birthday": "1990-01-01",
                                                    "gender": "MALE"
                                                }
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "중복된 이메일 또는 닉네임",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = {
                                        @ExampleObject(
                                                name = "이메일 중복",
                                                value = """
                                                {
                                                    "message": "이미 사용 중인 이메일입니다.",
                                                    "data": null
                                                }
                                                """
                                        ),
                                        @ExampleObject(
                                                name = "닉네임 중복",
                                                value = """
                                                {
                                                    "message": "이미 사용 중인 닉네임입니다.",
                                                    "data": null
                                                }
                                                """
                                        )
                                    }
                            )
                    )
            }
    )
    @PostMapping("/signup")
    ResponseEntity<CommonResponse<SignupResponse>> signup(@RequestBody SignupRequest request);

    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호로 로그인합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(
                                    name = "로그인 요청 예시",
                                    value = """
                                    {
                                        "email": "user@example.com",
                                        "password": "Password123!"
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
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "로그인 성공 응답 예시",
                                            value = """
                                            {
                                                "message": "로그인 성공",
                                                "data": {
                                                    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                                                }
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "로그인 실패 응답 예시",
                                            value = """
                                            {
                                                "message": "이메일 또는 비밀번호가 올바르지 않습니다.",
                                                "data": null
                                            }
                                            """
                                    )
                            )
                    )
            }
    )
    @PostMapping("/login")
    ResponseEntity<CommonResponse<TokenResponse>> login(@RequestBody LoginRequest request);

    @Operation(
            summary = "로그아웃",
            description = "현재 로그인된 사용자를 로그아웃합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "로그아웃 성공",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "로그아웃 성공 응답 예시",
                                            value = "{\"message\":\"로그아웃 성공\",\"data\":null}"
                                    )
                            )
                    )
            }
    )
    @PostMapping("/logout")
    ResponseEntity<CommonResponse<Void>> logout(@RequestHeader("Authorization") String token);
} 