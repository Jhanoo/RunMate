package com.runhwani.runmate.service;

import com.runhwani.runmate.dto.request.course.CourseRequest;
import com.runhwani.runmate.dto.response.course.CourseDetailResponse;
import com.runhwani.runmate.dto.response.course.CourseLikeResponse;
import com.runhwani.runmate.dto.response.course.CourseResponse;
import com.runhwani.runmate.exception.EntityNotFoundException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface CourseService {
    // 1. 코스 생성
    UUID createCourse(CourseRequest request, UUID userId) throws IOException;
    // 2. 코스 삭제
    void deleteCourse(UUID courseId, UUID userId)
        throws EntityNotFoundException, IOException;
    // 3. 코스 검색
    List<CourseResponse> searchCourses(UUID userId, String keyword);
    // 4. 최근 코스 조회
    List<CourseResponse> getRecentCourses(UUID userId);
    // 5. 내가 등록한 코스 조회
    List<CourseResponse> getCoursesCreatedBy(UUID userId);
    // 6. 모든 코스 조회
    List<CourseResponse> getAllCourses(UUID userId);
    // 7. 코스 상세 조회
    CourseDetailResponse getCourseDetail(UUID userId, UUID courseId);
    // 8. 코스 좋아요 업데이트
    CourseLikeResponse updateCourseLike(UUID userId, UUID courseId);
}
