package com.runhwani.runmate.service;

import com.runhwani.runmate.dao.CourseDao;
import com.runhwani.runmate.dto.request.course.CourseRequest;
import com.runhwani.runmate.model.Course;
import com.runhwani.runmate.utils.GpxStorageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
}