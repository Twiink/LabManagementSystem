package com.example.labmanagementsystembackend.mapper;

import com.example.labmanagementsystembackend.domain.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseMapper {
    Course findById(@Param("id") Long id);

    List<Course> findCourses(@Param("createdBy") Long createdBy,
                             @Param("term") String term,
                             @Param("offset") int offset,
                             @Param("pageSize") int pageSize);

    long countCourses(@Param("createdBy") Long createdBy, @Param("term") String term);

    int insertCourse(Course course);
}
