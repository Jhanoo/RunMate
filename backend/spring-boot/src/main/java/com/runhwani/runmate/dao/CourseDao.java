package com.runhwani.runmate.dao;

import com.runhwani.runmate.model.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

@Mapper
public interface CourseDao {
    void insertCourse(Course course);

    Course selectCourseById(@Param("courseId") UUID courseId);
    void deleteCourse(@Param("courseId") UUID courseId);
}
