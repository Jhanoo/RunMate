package com.runhwani.runmate.service;

import com.runhwani.runmate.dto.request.course.CourseRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public interface CourseService {
    UUID createCourse(CourseRequest request, MultipartFile gpxFile, UUID userId) throws IOException;
}
