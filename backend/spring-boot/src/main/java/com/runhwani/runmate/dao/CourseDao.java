package com.runhwani.runmate.dao;

import com.runhwani.runmate.dto.response.course.CourseResponse;
import com.runhwani.runmate.model.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    List<CourseResponse> findCoursesByIds(@Param("courseIds") List<UUID> courseIds, @Param("userId") UUID userId);
    // 5. 내가 등록한 코스 조회
    List<CourseResponse> findCoursesCreatedByUser(@Param("userId") UUID userId);
    // 6. 전체 코스 조회
    List<CourseResponse> findAllCourses(@Param("userId") UUID userId);
    // 7. 코스 상세 조회
    Optional<Course> findCourseById(@Param("courseId") UUID courseId);
    int countLikesByCourseId(@Param("courseId") UUID courseId);
    Double getAverageUserPace(); // 전체 사용자 페이스
    Double getUserPace(@Param("userId") UUID userId); // 특정 사용자 페이스
    // 8. 코스 좋아요 업데이트
    boolean existsCourseLike(@Param("userId") UUID userId, @Param("courseId") UUID courseId);
    void insertCourseLike(@Param("userId") UUID userId, @Param("courseId") UUID courseId);
    int deleteCourseLike(@Param("userId") UUID userId, @Param("courseId") UUID courseId);
}
