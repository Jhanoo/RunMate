package com.runhwani.runmate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runhwani.runmate.dto.common.CommonResponse;
import com.runhwani.runmate.dto.request.course.CourseRequest;
import com.runhwani.runmate.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Validated
@Slf4j
public class CourseController {

    private final CourseService courseService;
    private final ObjectMapper objectMapper;

    @Operation(
            summary = "코스 생성",
            description = "코스 정보(JSON)와 GPX 파일을 multipart/form-data로 업로드해 새 러닝 코스를 생성합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "코스 생성 성공")
            }
    )
    @PostMapping(
            path = "/create",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<CommonResponse<UUID>> createCourse(
            @RequestParam("courseData")
            @Parameter(
                    description = "코스 데이터 (JSON 형식)",
                    schema = @Schema(type = "string"),
                    examples = {
                            @ExampleObject(
                                    name = "기본 코스",
                                    summary = "기본 러닝 코스 예제",
                                    value = "{\"name\":\"한강 러닝 코스\",\"isShared\":true,\"distance\":5.2,\"avgElevation\":12.5,\"startLocation\":\"서울시 마포구 망원한강공원\"}"
                            ),
                            @ExampleObject(
                                    name = "산악 코스",
                                    summary = "산악 러닝 코스 예제",
                                    value = "{\"name\":\"북한산 트레일\",\"isShared\":true,\"distance\":8.7,\"avgElevation\":110.3,\"startLocation\":\"서울시 강북구 우이동\"}"
                            ),
                            @ExampleObject(
                                    name = "비공개 코스",
                                    summary = "비공개 코스 예제",
                                    value = "{\"name\":\"내 동네 러닝\",\"isShared\":false,\"distance\":3.1,\"avgElevation\":5.2,\"startLocation\":\"경기도 성남시 분당구\"}"
                            )
                    }
            ) String courseDataJson,
            @RequestPart(value = "gpxFile", required = false) MultipartFile gpxFile,
            HttpServletRequest request
    ) {
        log.debug("Request Content-Type: {}", request.getContentType());
        log.debug("CourseData JSON: {}", courseDataJson);
        log.debug("GPX File name: {}", gpxFile != null ? gpxFile.getOriginalFilename() : "null");

        CourseRequest courseData;
        try {
            courseData = objectMapper.readValue(courseDataJson, CourseRequest.class);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "courseData JSON 파싱 중 오류가 발생했습니다: " + e.getMessage(),
                    e
            );
        }

        UUID userId = UUID.fromString(request.getAttribute("userId").toString());
        UUID newCourseId;
        try {
            newCourseId = courseService.createCourse(courseData, gpxFile, userId);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "GPX 파일 저장 중 오류가 발생했습니다.",
                    e
            );
        }
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CommonResponse.ok(newCourseId));
    }
}