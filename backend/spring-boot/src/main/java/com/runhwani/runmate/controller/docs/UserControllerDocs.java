package com.runhwani.runmate.controller.docs;

import com.runhwani.runmate.dto.common.CommonResponse;
import com.runhwani.runmate.dto.response.user.UserProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "User", description = "사용자 관련 API")
@RequestMapping("/api/users")
public interface UserControllerDocs {

    @Operation(summary = "현재 로그인한 사용자 프로필 조회", description = "JWT 토큰을 통해 현재 로그인한 사용자의 프로필 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "프로필 조회 성공", 
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CommonResponse.class),
                    examples = @ExampleObject(value = "{\"message\":\"프로필 조회 성공\",\"data\":{\"email\":\"user@example.com\",\"nickname\":\"러너1\",\"birthday\":\"1990-01-01\",\"gender\":\"MALE\",\"profileImageUrl\":\"이미지URL\"}}"))),
        @ApiResponse(responseCode = "401", description = "인증 실패", 
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CommonResponse.class),
                    examples = @ExampleObject(value = "{\"message\":\"인증에 실패했습니다\",\"data\":null}"))),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", 
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CommonResponse.class),
                    examples = @ExampleObject(value = "{\"message\":\"사용자를 찾을 수 없습니다\",\"data\":null}")))
    })
    @GetMapping("/me")
    ResponseEntity<CommonResponse<UserProfileResponse>> getMyProfile();
} 