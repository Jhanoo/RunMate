package com.runhwani.runmate.service;

import com.runhwani.runmate.dao.CourseDao;
import com.runhwani.runmate.dto.request.course.CourseRequest;
import com.runhwani.runmate.model.Course;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${course.gpx.dir}") // 파일 저장 경로 application.yml에 명시
    private String gpxStoragePath;

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

        // 2. GPX 파일 처리 (있는 경우에만)
        String gpxFileName = null;
        if (gpxFile != null && !gpxFile.isEmpty()) {
            UUID fileUUID = UUID.randomUUID();
            gpxFileName = fileUUID + ".gpx";

            // 디렉토리 존재 확인 및 생성
            Path dirPath = Paths.get(gpxStoragePath);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // 파일 저장 경로 구성
            Path gpxPath = Paths.get(gpxStoragePath, gpxFileName);
            Files.copy(gpxFile.getInputStream(), gpxPath, StandardCopyOption.REPLACE_EXISTING);
            log.debug("GPX 파일 저장 완료: {}", gpxPath);
        }
        course.setGpxFile(gpxFileName);

        // 3. DB에 저장
        courseDao.insertCourse(course);
        log.debug("코스 DB 저장 완료: {}", course.getCourseId());

        return course.getCourseId();
    }
}