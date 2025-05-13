package com.runhwani.runmate.controller.docs;

import com.runhwani.runmate.dto.common.CommonResponse;
import com.runhwani.runmate.dto.request.curriculum.CurriculumCreateRequest;
import com.runhwani.runmate.dto.response.curriculum.CurriculumCreateResponse;
import com.runhwani.runmate.dto.response.curriculum.TodoResponse;
import com.runhwani.runmate.model.Curriculum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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


    @Operation(
            summary = "커리큘럼 조회",
            description = "사용자의 커리큘럼 반환",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "커리큘럼이 존재하지 않음",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "NotFound",
                                            value = "{\"message\": \"생성된 커리큘럼이 없습니다.\", \"data\": null}"
                                    )
                            )
                    )
            }
    )
    @GetMapping("/my")
    ResponseEntity<CommonResponse<Curriculum>> getCurriculum(
            @AuthenticationPrincipal UserDetails principal
    );

    @Operation(
            summary = "월별 ToDo 조회",
            description = "로그인한 사용자의 커리큘럼에서, 요청한 연·월에 해당하는 ToDo 목록을 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TodoResponse.class)
                            )
                    )
            }
    )
    @GetMapping("/todoList")
    ResponseEntity<CommonResponse<List<TodoResponse>>> getTodosByMonth(
            @Parameter(description = "조회할 연도", example = "2025")
            @RequestParam("year") int year,
            @Parameter(description = "조회할 월 (1~12)", example = "5")
            @RequestParam("month") int month,
            @AuthenticationPrincipal UserDetails principal
    );
}
