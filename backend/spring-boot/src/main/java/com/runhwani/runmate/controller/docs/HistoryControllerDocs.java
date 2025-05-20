package com.runhwani.runmate.controller.docs;

import com.runhwani.runmate.dto.common.CommonResponse;
import com.runhwani.runmate.dto.response.history.HistoryDetailResponse;
import com.runhwani.runmate.dto.response.history.HistoryListResponse;
import com.runhwani.runmate.dto.response.history.RunnerDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Tag(name = "히스토리", description = "달리기 기록(히스토리) 관련 API")
public interface HistoryControllerDocs {

    @Operation(summary = "히스토리 목록 조회", description = "내 달리기 기록(히스토리)을 페이징하여 가져옵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "히스토리 목록 조회 성공"),
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
            @ApiResponse(responseCode = "200", description = "히스토리 상세 조회 성공"),
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

    @Operation(
            summary = "히스토리 참여자 기록 상세 조회",
            description = "특정 히스토리 내에서 특정 사용자의 주행 기록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "히스토리 참여자 기록 상세 조회 성공 응답 예시",
                                            value = """
                                                    {
                                                      "message": "히스토리 참여자 기록 상세 조회 성공",
                                                      "data": {
                                                        "userId": "u1234",
                                                        "nickname": "달려라 후니",
                                                        "profileImage": "https://runmate.com/images/u1234.jpg",
                                                        "startTime": "2025-05-10T07:30:00",
                                                        "endTime": "2025-05-10T08:00:00",
                                                        "distance": 10.0,
                                                        "gpxFile": "https://runmate-files.s3.amazonaws.com/gpx/abc123.gpx",
                                                        "calories": 420,
                                                        "avgCadence": 160,
                                                        "avgPace": 330,
                                                        "avgBpm": 150,
                                                        "avgElevation": 9.7
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
                                            name = "인증 실패 응답 예시",
                                            value = """
                                                    {
                                                      "message": "인증에 실패했습니다",
                                                      "data": null
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "기록을 찾을 수 없음",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "기록을 찾을 수 없음 응답 예시",
                                            value = """
                                                    {
                                                      "message": "해당 사용자의 기록을 찾을 수 없습니다.",
                                                      "data": null
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    ResponseEntity<CommonResponse<RunnerDetailResponse>> getRunnerDetail(
            @PathVariable UUID historyId,
            @PathVariable UUID userId
    );
} 