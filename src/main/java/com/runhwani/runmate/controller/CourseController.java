package com.runhwani.runmate.controller;

import com.runhwani.runmate.controller.docs.CourseControllerDocs;
import com.runhwani.runmate.dto.common.CommonResponse;
import com.runhwani.runmate.dto.request.course.CourseRequest;
import com.runhwani.runmate.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Validated
@Slf4j
public class CourseController implements CourseControllerDocs {

    private final CourseService courseService;

    public ResponseEntity<CommonResponse<UUID>> createCourse(
            @RequestPart("courseData") CourseRequest courseData,
            @RequestPart(value = "gpxFile", required = false) MultipartFile gpxFile
    ) {
        // ---여기선 이미 courseData가 파싱된 상태입니다---
        log.debug("GPX File name: {}", gpxFile != null ? gpxFile.getOriginalFilename() : "null");

//        UUID userId = UUID.fromString(request.getAttribute("userId").toString());
        UUID userId = UUID.fromString("19caf646-e6b5-496a-90d9-25e8a84f2646");
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