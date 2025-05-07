package com.runhwani.runmate.controller.docs;

import com.runhwani.runmate.dto.common.CommonResponse;
import com.runhwani.runmate.dto.request.group.GroupRequest;
import com.runhwani.runmate.dto.response.group.GroupResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Swagger 문서 전용 인터페이스: 그룹 생성 API
 */
@Tag(name = "그룹", description = "그룹 생성 및 조회 API")
@RequestMapping("/api/groups/create")
public interface GroupControllerDocs {

    @Operation(
            summary = "그룹 생성",
            description = "그룹명, 코스 ID(선택), 시작 일시 및 위치 정보를 입력받아 새 달리기 그룹을 생성합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GroupRequest.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "그룹 생성 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CommonResponse.class)
                            )
                    )
            }
    )
    @PostMapping
    ResponseEntity<CommonResponse<GroupResponse>> createGroup(
            @org.springframework.web.bind.annotation.RequestBody GroupRequest request
    );
}
