package com.runhwani.runmate.dao;

import com.runhwani.runmate.model.Course;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourseDao {
    void insertCourse(Course course);
}
