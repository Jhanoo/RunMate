package com.runhwani.runmate.controller.docs;

import com.runhwani.runmate.dto.common.CommonResponse;
import com.runhwani.runmate.dto.request.group.GroupRequest;
import com.runhwani.runmate.dto.response.group.GroupResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Swagger 문서 전용 인터페이스: 그룹 생성 API
 */
@Tag(name = "4. 그룹", description = "그룹 생성 및 조회 API")
@RequestMapping("/api/groups/create")
public interface GroupControllerDocs {

    @Operation(
            summary = "그룹 생성",
            description = "그룹명, 코스 ID(선택), 시작 일시 및 위치 정보를 입력받아 새 달리기 그룹을 생성합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GroupRequest.class),
                            examples = @ExampleObject(
                                    name = "생성 요청 예시",
                                    value = "{\"groupName\": \"한강 러닝 모임\", \"courseId\": null, " +
                                            "\"startTime\": \"2025-05-10T08:00:00\", \"startLocation\": \"여의도 한강공원\", " +
                                            "\"latitude\": 37.533422, \"longitude\": 126.896495}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "그룹 생성 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "성공 응답 예시",
                                            value = "{\"message\": \"요청 성공\", \"data\": {\"groupId\": \"4f5a6b7c-8d9e-0f1a-2b3c-4d5e6f7a8b9c\", " +
                                                    "\"inviteCode\": \"a1b2c3d4\", \"groupName\": \"한강 러닝 모임\", " +
                                                    "\"courseId\": null, \"startTime\": \"2025-05-10T08:00:00\", " +
                                                    "\"startLocation\": \"여의도 한강공원\", \"latitude\": 37.533422, " +
                                                    "\"longitude\": 126.896495}}"
                                    )
                            )
                    )
            }
    )
    @PostMapping
    ResponseEntity<CommonResponse<GroupResponse>> createGroup(
            @org.springframework.web.bind.annotation.RequestBody GroupRequest request
    );
}
