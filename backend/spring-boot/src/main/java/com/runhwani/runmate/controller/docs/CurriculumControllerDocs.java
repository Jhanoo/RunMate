package com.runhwani.runmate.controller.docs;

import com.runhwani.runmate.dto.common.CommonResponse;
import com.runhwani.runmate.dto.request.curriculum.CurriculumCreateRequest;
import com.runhwani.runmate.dto.response.curriculum.CurriculumCreateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/curricula")
@Tag(name = "커리큘럼", description = "커리큘럼 관리 API")
public interface CurriculumControllerDocs {

    @Operation(
            summary = "커리큘럼 생성",
            description = "사용자 데이터를 바탕으로 AI 호출 → 날짜별 ToDo 생성",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CurriculumCreateRequest.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "커리큘럼 생성 성공"
                    )
            }
    )
    @PostMapping("/create")
    ResponseEntity<CommonResponse<CurriculumCreateResponse>> createCurriculum(
            @RequestBody CurriculumCreateRequest request,
            @AuthenticationPrincipal UserDetails principal);

}
