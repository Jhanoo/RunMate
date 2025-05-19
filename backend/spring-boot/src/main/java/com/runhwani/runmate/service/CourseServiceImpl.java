package com.runhwani.runmate.service;

import com.runhwani.runmate.dao.CourseDao;
import com.runhwani.runmate.dao.HistoryDao;
import com.runhwani.runmate.dto.request.course.CourseRequest;
import com.runhwani.runmate.dto.response.course.CourseDetailResponse;
import com.runhwani.runmate.dto.response.course.CourseLikeResponse;
import com.runhwani.runmate.dto.response.course.CourseResponse;
import com.runhwani.runmate.exception.EntityNotFoundException;
import com.runhwani.runmate.model.Course;
import com.runhwani.runmate.model.History;
import com.runhwani.runmate.utils.GpxStorageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseServiceImpl implements CourseService {
    private final CourseDao courseDao;
    private final HistoryDao historyDao;

    @Override
    @Transactional
    public UUID createCourse(CourseRequest request, UUID userId) throws IOException {
        log.debug("코스 생성 요청: {}", request);

        // 1. Course 객체 생성
        Course course = new Course();
        course.setCourseId(UUID.randomUUID());
        course.setCourseName(request.getName());
        course.setIsShared(request.isShared());
        course.setDistance(request.getDistance());
        course.setAvgElevation(request.getAvgElevation());
        course.setStartLocation(request.getStartLocation());
        course.setCreatedBy(userId);

        // 2. GPX 파일 처리: 유틸 호출 -> history 조회 후 가져오기
        History history = historyDao.selectHistoryById(request.getHistoryId())
                .orElseThrow(() -> new EntityNotFoundException("헤당 히스토리를 찾을 수 없습니다."));
        String gpxFileName = history.getGpxFile();
        course.setGpxFile(gpxFileName);
        log.debug("히스토리에서 가져온 GPX 파일명: {}", gpxFileName);

        // 3. Course DB에 저장
        courseDao.insertCourse(course);
        log.debug("코스 DB 저장 완료: {}", course.getCourseId());

        // 4. 생성된 코스 ID로 해당 히스토리의 course_id 업데이트
        historyDao.updateHistoryCourseId(request.getHistoryId(), course.getCourseId());
        log.debug("히스토리 업데이트 완료: historyId={}, courseId={}",
                request.getHistoryId(), course.getCourseId());

        return course.getCourseId();
    }

    @Override
    @Transactional
    public void deleteCourse(UUID courseId, UUID userId) throws IOException {
        // 1. DB 코스 조회
        Course course = courseDao.selectCourseById(courseId);
        if (course == null) {
            throw new EntityNotFoundException("코스를 찾을 수 없습니다. : " + courseId);
        }

        // 2. 소유자 검증
        if (!course.getCreatedBy().equals(userId)) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }

        // 3. 히스토리에서 참조 끊기: course_id를 NULL로 업데이트
        courseDao.nullifyCourseReferenceInHistories(courseId);

        // 4. gpx 파일 삭제
        String gpxFileName = course.getGpxFile();
        GpxStorageUtil.deleteGpxFile(gpxFileName);

        // 5. db 레코드 삭제
        courseDao.deleteCourse(courseId);
        log.debug("코스 db 삭제 완료: {}", courseId);
    }

    @Override
    public List<CourseResponse> searchCourses(UUID userId, String keyword) {
        // 빈 검색어 방지 로직
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("검색어가 공백입니다.");
        }

        String pattern = "%" + keyword + "%";
        Map<String, Object> params = new HashMap<>();
        params.put("name", pattern);
        params.put("nickname", pattern);
        params.put("location", pattern);
        params.put("userId", userId);

        return courseDao.searchCourses(params);
    }

    // 4. 최근 코스 조회
    @Override
    public List<CourseResponse> getRecentCourses(UUID userId) {
        // 1달 이내 코스 ID 조회
        List<UUID> recentCourseIds = courseDao.findRecentCourseIds(userId);
        if (recentCourseIds.isEmpty()) return Collections.emptyList();

        // 코스 상세 정보 조회
        return courseDao.findCoursesByIds(recentCourseIds, userId);
    }

    // 5. 내가 등록한 코스 조회
    @Override
    public List<CourseResponse> getCoursesCreatedBy(UUID userId) {
        return courseDao.findCoursesCreatedByUser(userId);
    }

    // 6. 코스 전체 조회
    @Override
    public List<CourseResponse> getAllCourses(UUID userId) {
        return courseDao.findAllCourses(userId);
    }

    // 7. 코스 상세 조회
    @Override
    public CourseDetailResponse getCourseDetail(UUID userId, UUID courseId) {
        // 1. 코스 정보 + GPX + 좋아요 수 조회
        Course course = courseDao.findCourseById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("해당 코스가 존재하지 않습니다."));

        int likeCount = courseDao.countLikesByCourseId(courseId);
        boolean liked = courseDao.existsCourseLike(userId, courseId);

        // 2. 평균 페이스 조회
        Double avgPace = 6.5; // 전체 사용자
        Double userPace = courseDao.getUserPace(userId); // 로그인 사용자

        Integer avgEstimatedTime = course.getDistance() != null
                ? (int) (avgPace * course.getDistance() * 60)
                : null;
        Integer userEstimatedTime = (userPace != null && course.getDistance() != null)
                ? (int) (userPace * course.getDistance() * 60)
                : null;

        double safeDistance = (course.getDistance() != null) ? course.getDistance() : 0.0;
        double safeElevation = (course.getAvgElevation() != null) ? course.getAvgElevation() : 0.0;

        boolean isShared = Boolean.TRUE.equals(course.getIsShared());

        // 3. 응답 객체 생성
        return CourseDetailResponse.builder()
                .id(course.getCourseId())
                .name(course.getCourseName())
                .isShared(isShared)
                .distance(safeDistance)
                .avgElevation(safeElevation)
                .startLocation(course.getStartLocation())
                .gpxFile(course.getGpxFile())
                .avgEstimatedTime(avgEstimatedTime)
                .userEstimatedTime(userEstimatedTime)
                .likes(likeCount)
                .liked(liked)
                .build();
    }

    // 8. 코스 좋아요 업데이트
    @Override
    @Transactional
    public CourseLikeResponse updateCourseLike(UUID userId, UUID courseId) {
        // 1. 코스 존재 확인
        Course course = courseDao.findCourseById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("해당 코스가 존재하지 않습니다."));
        // 2. 기존 좋아요 여부 확인
        boolean exists = courseDao.existsCourseLike(userId, courseId);
        // 3. 좋아요 있 -> 삭제, 없 -> 삽입
        if (exists) {
            courseDao.deleteCourseLike(userId, courseId);
        } else {
            courseDao.insertCourseLike(userId, courseId);
        }
        // 4. 최종 좋아요 개수
        int total = courseDao.countLikesByCourseId(courseId);
        // 5. 응답 반환
        return CourseLikeResponse.builder()
                .liked(!exists)
                .totalLikes(total)
                .build();
    }

    @Override
    @Transactional
    public Course getCourseByCourseId(UUID courseId) {
        return courseDao.selectCourseById(courseId);
    }
}