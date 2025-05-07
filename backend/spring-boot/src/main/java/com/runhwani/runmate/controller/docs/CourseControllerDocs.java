package com.runhwani.runmate.controller.docs;

import com.runhwani.runmate.dto.common.CommonResponse;
import com.runhwani.runmate.dto.request.course.CourseRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.StringToClassMapItem;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@Tag(name = "코스", description = "코스 생성 및 조회 API")
@RequestMapping("/api/courses")
public interface CourseControllerDocs {

    // 1. 코스 생성
    @Operation(
            summary = "코스 생성",
            description = "코스 정보(JSON)와 GPX 파일을 multipart/form-data로 업로드해 새 러닝 코스를 생성합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(
                                    type = "object",
                                    // multipart/form-data 각 파트 스펙을 StringToClassMapItem 으로 정의
                                    properties = {
                                            @StringToClassMapItem(key = "courseData", value = CourseRequest.class),
                                            @StringToClassMapItem(key = "gpxFile", value = File.class)
                                    },
                                    requiredProperties = {"courseData"}
                            ), encoding = {
                            @Encoding(name = "courseData",
                                    contentType = MediaType.APPLICATION_JSON_VALUE),
                            @Encoding(name = "gpxFile",
                                    contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
                            }
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "코스 생성 성공",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "성공 응답 예시",
                                            value = "{\"message\":\"요청 성공\",\"data\":\"19caf646-e6b5-496a-90d9-25e8a84f2646\"}"
                                    )
                            )
                    )
            }
    )
    @PostMapping("/create")
    ResponseEntity<CommonResponse<UUID>> createCourse(
            @Parameter(hidden = true) UserDetails principal,
            @RequestPart("courseData") CourseRequest courseData,
            @RequestPart(value = "gpxFile", required = false) MultipartFile gpxFile
    );

    @Operation(
            summary = "코스 삭제",
            description = "경로 ID로 코스를 삭제합니다. DB 레코드를 제거하고, 연관된 GPX 파일을 EC2에서 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공")
            }
    )
    @DeleteMapping("/{courseId}")
    ResponseEntity<CommonResponse<Void>> deleteCourse(
            @Parameter(hidden = true) UserDetails principal,
            @PathVariable("courseId") UUID courseId
    );
}
