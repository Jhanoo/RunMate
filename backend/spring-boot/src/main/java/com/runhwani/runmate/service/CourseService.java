package com.runhwani.runmate.service;

import com.runhwani.runmate.dto.request.course.CourseRequest;
import com.runhwani.runmate.dto.response.course.CourseResponse;
import com.runhwani.runmate.exception.EntityNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

public interface CourseService {
    UUID createCourse(CourseRequest request, MultipartFile gpxFile, UUID userId) throws IOException;

    void deleteCourse(UUID courseId, UUID userId)
        throws EntityNotFoundException, IOException;

    List<CourseResponse> searchCourses(String keyword);
}
