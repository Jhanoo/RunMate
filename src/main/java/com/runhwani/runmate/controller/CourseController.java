package com.runhwani.runmate.controller;

import com.runhwani.runmate.controller.docs.CourseControllerDocs;
import com.runhwani.runmate.dto.common.CommonResponse;
import com.runhwani.runmate.dto.request.course.CourseRequest;
import com.runhwani.runmate.exception.CustomException;
import com.runhwani.runmate.exception.EntityNotFoundException;
import com.runhwani.runmate.exception.ErrorCode;
import com.runhwani.runmate.security.jwt.JwtProvider;
import com.runhwani.runmate.service.CourseService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
public class CourseController implements CourseControllerDocs {

    private final CourseService courseService;

    @Override
    public ResponseEntity<CommonResponse<UUID>> createCourse(
            @AuthenticationPrincipal UserDetails principal,
            @RequestPart("courseData") CourseRequest courseData,
            @RequestPart(value = "gpxFile", required = false) MultipartFile gpxFile
    ) {
        // ---여기선 이미 courseData가 파싱된 상태입니다---
        log.debug("GPX File name: {}", gpxFile != null ? gpxFile.getOriginalFilename() : "null");


        UUID userId = UUID.fromString(principal.getUsername());

        try {
            UUID newCourseId = courseService.createCourse(courseData, gpxFile, userId);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(CommonResponse.ok(newCourseId));
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "GPX 파일 저장 중 오류가 발생했습니다.",
                    e
            );
        }
    }

    @Override
    public ResponseEntity<CommonResponse<Void>> deleteCourse(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID courseId
    ) {
        UUID userId = UUID.fromString(principal.getUsername());
        try {
            courseService.deleteCourse(courseId, userId);
            return ResponseEntity.ok(CommonResponse.ok(null));
        } catch (EntityNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(CommonResponse.error("존재하지 않는 코스입니다."));
        } catch (AccessDeniedException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(CommonResponse.error("삭제 권한이 없습니다."));
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.error("GPX 파일 삭제 중 오류가 발생했습니다."));
        }
    }



}