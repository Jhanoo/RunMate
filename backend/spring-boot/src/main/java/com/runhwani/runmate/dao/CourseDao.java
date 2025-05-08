package com.runhwani.runmate.dao;

import com.runhwani.runmate.dto.response.course.CourseResponse;
import com.runhwani.runmate.model.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mapper
public interface CourseDao {
    // 1. 코스 생성
    void insertCourse(Course course);
    // 2. 코스 삭제
    Course selectCourseById(@Param("courseId") UUID courseId);
    void deleteCourse(@Param("courseId") UUID courseId);
    // 3. 코스 검색
    List<CourseResponse> searchCourses(Map<String, Object> params);
    // 4. 최근 코스 조회
    List<UUID> findRecentCourseIds(@Param("userId") UUID userId);
    List<CourseResponse> findCoursesByIds(@Param("courseIds") List<UUID> courseIds);
    // 5. 내가 등록한 코스 조회
    List<CourseResponse> findCoursesCreatedByUser(@Param("userId") UUID userId);
    // 6. 전체 코스 조회
    List<CourseResponse> findAllCourses();
}
