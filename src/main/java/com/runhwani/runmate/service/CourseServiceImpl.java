package com.runhwani.runmate.service;

import com.runhwani.runmate.dao.CourseDao;
import com.runhwani.runmate.dto.request.course.CourseRequest;
import com.runhwani.runmate.exception.EntityNotFoundException;
import com.runhwani.runmate.model.Course;
import com.runhwani.runmate.utils.GpxStorageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseServiceImpl implements CourseService {
    private final CourseDao courseDao;

    @Override
    public UUID createCourse(CourseRequest request, MultipartFile gpxFile, UUID userId) throws IOException {
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

        // 2. GPX 파일 처리: 유틸 호출
        String gpxFileName = GpxStorageUtil.saveGpxFile(gpxFile);
        course.setGpxFile(gpxFileName);
        log.debug("저장된 GPX 파일명: {}", gpxFileName);

        // 3. DB에 저장
        courseDao.insertCourse(course);
        log.debug("코스 DB 저장 완료: {}", course.getCourseId());

        return course.getCourseId();
    }

    @Override
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

        // 3. gpx 파일 삭제
        String gpxFileName = course.getGpxFile();
        if (gpxFileName != null && !gpxFileName.isBlank()) {
            Path path = Paths.get(gpxStoragePath).resolve(gpxFileName);
            log.debug("gpx 파일 삭제 시도 : {}", path);
            Files.deleteIfExists(path);
        }

        // 4. db 레코드 삭제
        courseDao.deleteCourse(courseId);
        log.debug("코스 db 삭제 완료: {}", courseId);
    }
}