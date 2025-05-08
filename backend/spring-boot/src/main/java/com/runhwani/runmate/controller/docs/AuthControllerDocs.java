package com.runhwani.runmate.controller.docs;

import com.runhwani.runmate.dto.common.CommonResponse;
import com.runhwani.runmate.dto.request.auth.LoginRequest;
import com.runhwani.runmate.dto.request.auth.SignupRequest;
import com.runhwani.runmate.dto.response.auth.SignupResponse;
import com.runhwani.runmate.dto.response.auth.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.StringToClassMapItem;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Tag(name = "인증", description = "인증 관련 API")
public interface AuthControllerDocs {

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
    @PostMapping("/api/auth/login")
    ResponseEntity<CommonResponse<TokenResponse>> login(@RequestBody LoginRequest request);

    @Operation(
            summary = "회원가입",
            description = "새로운 사용자를 등록합니다. 프로필 이미지는 선택적으로 업로드할 수 있습니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(
                                    type = "object",
                                    properties = {
                                            @StringToClassMapItem(key = "email", value = String.class),
                                            @StringToClassMapItem(key = "password", value = String.class),
                                            @StringToClassMapItem(key = "nickname", value = String.class),
                                            @StringToClassMapItem(key = "birthday", value = String.class),
                                            @StringToClassMapItem(key = "gender", value = String.class),
                                            @StringToClassMapItem(key = "profileImage", value = File.class)
                                    },
                                    requiredProperties = {"email", "password", "nickname"}
                            ),
                            encoding = {
                                    @Encoding(name = "email", contentType = "text/plain"),
                                    @Encoding(name = "password", contentType = "text/plain"),
                                    @Encoding(name = "nickname", contentType = "text/plain"),
                                    @Encoding(name = "birthday", contentType = "text/plain"),
                                    @Encoding(name = "gender", contentType = "text/plain"),
                                    @Encoding(name = "profileImage", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
                            }
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
                                                    "nickname": "러너1",
                                                    "profileImage": "http://localhost:8080/uploads/12345.jpg",
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
    @PostMapping(value = "/api/auth/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<CommonResponse<SignupResponse>> signup(
            @Parameter(
                    in = ParameterIn.DEFAULT,
                    name = "email",
                    description = "이메일",
                    required = true,
                    schema = @Schema(type = "user@example.com", example = "user@example.com", defaultValue = "user@example.com"),
                    content = @Content(mediaType = "text/plain")
            )
            @RequestPart("email") String email,
            
            @Parameter(
                    in = ParameterIn.DEFAULT,
                    name = "password",
                    description = "비밀번호",
                    required = true,
                    schema = @Schema(type = "string", example = "Password123!", defaultValue = "Password123!"),
                    content = @Content(mediaType = "text/plain")
            )
            @RequestPart("password") String password,
            
            @Parameter(
                    in = ParameterIn.DEFAULT,
                    name = "nickname",
                    description = "닉네임",
                    required = true,
                    schema = @Schema(type = "string", example = "러너1", defaultValue = "러너1"),
                    content = @Content(mediaType = "text/plain")
            )
            @RequestPart("nickname") String nickname,
            
            @Parameter(
                    in = ParameterIn.DEFAULT,
                    name = "birthday",
                    description = "생일 (YYYY-MM-DD 형식)",
                    required = false,
                    schema = @Schema(type = "string", example = "1990-01-01", defaultValue = "1990-01-01"),
                    content = @Content(mediaType = "text/plain")
            )
            @RequestPart(value = "birthday", required = false) String birthday,
            
            @Parameter(
                    in = ParameterIn.DEFAULT,
                    name = "gender",
                    description = "성별 (MALE, FEMALE)",
                    required = false,
                    schema = @Schema(type = "string", example = "MALE", defaultValue = "MALE"),
                    content = @Content(mediaType = "text/plain")
            )
            @RequestPart(value = "gender", required = false) String gender,
            
            @Parameter(
                    in = ParameterIn.DEFAULT,
                    name = "profileImage",
                    description = "프로필 이미지 파일 (JPG, PNG)",
                    required = false,
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
            )
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    );

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
    @PostMapping("/api/auth/logout")
    ResponseEntity<CommonResponse<Void>> logout(@RequestHeader("Authorization") String token);
} 