package com.runhwani.runmate.controller.docs;

import com.runhwani.runmate.dto.common.CommonResponse;
import com.runhwani.runmate.dto.response.history.HistoryDetailResponse;
import com.runhwani.runmate.dto.response.history.HistoryListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Tag(name = "History", description = "달리기 기록(히스토리) 관련 API")
public interface HistoryControllerDocs {

    @Operation(summary = "히스토리 목록 조회", description = "내 달리기 기록(히스토리)을 페이징하여 가져옵니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "히스토리 목록 조회 성공", 
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CommonResponse.class),
                    examples = @ExampleObject(value = "{\"message\":\"히스토리 목록 조회 성공\",\"data\":{\"total\":50,\"page\":1,\"size\":10,\"histories\":[{\"historyId\":\"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\"courseName\":\"한강 러닝\",\"groupName\":\"월요 모임\",\"location\":\"반포 한강공원\",\"startTime\":\"2025-05-10T07:00:00Z\",\"duration\":3600,\"members\":[\"https://…/alice.jpg\",\"https://…/bob.jpg\"],\"myDistance\":10.3}]}}"))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", 
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CommonResponse.class),
                    examples = @ExampleObject(value = "{\"message\":\"잘못된 요청입니다. 입력 값을 확인해주세요.\",\"data\":null}"))),
        @ApiResponse(responseCode = "401", description = "인증 실패", 
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CommonResponse.class),
                    examples = @ExampleObject(value = "{\"message\":\"인증되지 않은 사용자입니다. 로그인 후 다시 시도하세요.\",\"data\":null}"))),
        @ApiResponse(responseCode = "500", description = "서버 오류", 
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CommonResponse.class),
                    examples = @ExampleObject(value = "{\"message\":\"서버 내부 오류가 발생했습니다. 관리자에게 문의하세요.\",\"data\":null}")))
    })
    ResponseEntity<CommonResponse<HistoryListResponse>> getHistoryList(
            @Parameter(description = "페이지 번호 (기본값: 1)", example = "1")
            @RequestParam(required = false, defaultValue = "1") int page,
            @Parameter(description = "페이지 크기 (기본값: 10)", example = "10")
            @RequestParam(required = false, defaultValue = "10") int size);
    
    @Operation(summary = "히스토리 상세 조회", description = "특정 달리기 기록의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "히스토리 상세 조회 성공", 
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CommonResponse.class),
                    examples = @ExampleObject(value = "{\"message\":\"히스토리 상세 조회 성공\",\"data\":{\"historyId\":\"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\"groupRun\":[{\"userId\":\"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\"nickname\":\"alice\",\"distance\":5.0,\"time\":1800,\"avgPace\":360.0},{\"userId\":\"3fa85f64-5717-4562-b3fc-2c963f66afa7\",\"nickname\":\"bob\",\"distance\":5.0,\"time\":1900,\"avgPace\":380.0}],\"myRun\":{\"distance\":5.0,\"time\":1800,\"avgPace\":360.0,\"avgBpm\":150.0,\"calories\":300.0,\"addedToCourse\":true}}}"))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", 
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CommonResponse.class),
                    examples = @ExampleObject(value = "{\"message\":\"잘못된 요청입니다. 입력 값을 확인해주세요.\",\"data\":null}"))),
        @ApiResponse(responseCode = "401", description = "인증 실패", 
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CommonResponse.class),
                    examples = @ExampleObject(value = "{\"message\":\"인증되지 않은 사용자입니다. 로그인 후 다시 시도하세요.\",\"data\":null}"))),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음", 
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CommonResponse.class),
                    examples = @ExampleObject(value = "{\"message\":\"해당 기록에 접근 권한이 없습니다\",\"data\":null}"))),
        @ApiResponse(responseCode = "404", description = "히스토리 정보 없음", 
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CommonResponse.class),
                    examples = @ExampleObject(value = "{\"message\":\"히스토리 정보를 찾을 수 없습니다\",\"data\":null}"))),
        @ApiResponse(responseCode = "500", description = "서버 오류", 
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CommonResponse.class),
                    examples = @ExampleObject(value = "{\"message\":\"서버 내부 오류가 발생했습니다. 관리자에게 문의하세요.\",\"data\":null}")))
    })
    ResponseEntity<CommonResponse<HistoryDetailResponse>> getHistoryDetail(
            @Parameter(description = "히스토리 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID historyId);
} 