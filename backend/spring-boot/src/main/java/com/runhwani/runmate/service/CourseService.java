package com.runhwani.runmate.service;

import com.runhwani.runmate.dto.request.course.CourseRequest;
import com.runhwani.runmate.dto.response.course.CourseResponse;
import com.runhwani.runmate.exception.EntityNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface CourseService {
    // 1. 코스 생성
    UUID createCourse(CourseRequest request, MultipartFile gpxFile, UUID userId) throws IOException;
    // 2. 코스 삭제
    void deleteCourse(UUID courseId, UUID userId)
        throws EntityNotFoundException, IOException;
    // 3. 코스 검색
    List<CourseResponse> searchCourses(String keyword);
    // 4. 최근 코스 조회
    List<CourseResponse> getRecentCourses(UUID userId);
}
