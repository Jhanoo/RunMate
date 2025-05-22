package com.runhwani.runmate.controller.docs;

import com.runhwani.runmate.dto.common.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "알림", description = "알림 관련 API")
public interface NotificationControllerDocs {

    @Operation(
            summary = "테스트 알림 전송",
            description = "특정 사용자에게 테스트 알림을 전송합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "알림 전송 성공",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "성공 응답 예시",
                                            value = "{\"message\":\"알림 전송 성공\",\"data\":true}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "알림 전송 실패 - FCM 토큰이 없거나 유효하지 않음",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "실패 응답 예시",
                                            value = "{\"message\":\"알림 전송 실패 - FCM 토큰이 없거나 유효하지 않습니다\",\"data\":false}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 오류",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "서버 오류 응답 예시",
                                            value = "{\"message\":\"알림 전송 중 오류 발생: 상세 오류 메시지\",\"data\":false}"
                                    )
                            )
                    )
            }
    )
    @PostMapping("/test/{userId}")
    ResponseEntity<CommonResponse<Boolean>> testNotification(
            @Parameter(description = "알림을 받을 사용자 ID", required = true)
            @PathVariable UUID userId
    );

    @Operation(
            summary = "오늘의 할 일 알림 전송",
            description = "특정 사용자에게 오늘의 할 일 알림을 전송합니다. 알림 로그 예시:\n\n" +
                    "**등록된 일정이 있는 경우:**\n" +
                    "```\n" +
                    "===== FCM 알림 전송 시작 =====\n" +
                    "대상 토큰: eWNgv6Zv6unsv-vfgu8qmT:APA91bG6cvQ9ykpQicX_ZvlDXYCDTLeXh7wmvVfB7mL-ogEPigmtfuU3A6QZ4y_lcqMIf1RTWbU4bIOrbBZpWfmCzxGYrPlkTlvgZmrlfFvbrIDTSCs5_3g\n" +
                    "알림 제목: 오늘의 할 일 알림\n" +
                    "알림 내용: 3km 낮은 강도의 러닝으로 회복에 집중하세요.\n" +
                    "추가 데이터: {type=TODO, todoId=eb8178ff-f852-48de-9c6b-69f3774d02dc, curriculumId=34033e75-aa50-400f-8ae3-ffbc31b9e2a9}\n" +
                    "```\n\n" +
                    "**등록된 일정이 없는 경우:**\n" +
                    "```\n" +
                    "===== FCM 알림 전송 시작 =====\n" +
                    "대상 토큰: eWNgv6Zv6unsv-vfgu8qmT:APA91bG6cvQ9ykpQicX_ZvlDXYCDTLeXh7wmvVfB7mL-ogEPigmtfuU3A6QZ4y_lcqMIf1RTWbU4bIOrbBZpWfmCzxGYrPlkTlvgZmrlfFvbrIDTSCs5_3g\n" +
                    "알림 제목: 오늘의 할 일\n" +
                    "알림 내용: 나만의 러닝 커리큘럼을 생성해 보세요!\n" +
                    "추가 데이터: {type=TODO_EMPTY}\n" +
                    "```",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "할 일 알림 전송 성공",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "성공 응답 예시",
                                            value = "{\"message\":\"오늘의 할 일 알림 전송 완료\",\"data\":true}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "알림 전송 실패 - FCM 토큰이 없거나 유효하지 않음",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "실패 응답 예시",
                                            value = "{\"message\":\"알림 전송 실패 - FCM 토큰이 없거나 유효하지 않습니다\",\"data\":false}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 오류",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "서버 오류 응답 예시",
                                            value = "{\"message\":\"할 일 알림 전송 중 오류 발생: 상세 오류 메시지\",\"data\":false}"
                                    )
                            )
                    )
            }
    )
    @PostMapping("/todos/{userId}")
    ResponseEntity<CommonResponse<Boolean>> sendTodayTodoNotifications(
            @Parameter(description = "알림을 받을 사용자 ID", required = true)
            @PathVariable UUID userId
    );
} 