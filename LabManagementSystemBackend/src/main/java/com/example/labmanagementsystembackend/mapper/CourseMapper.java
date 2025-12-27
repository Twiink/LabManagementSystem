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

    /**
     * 更新课程信息
     */
    int updateCourse(Course course);

    /**
     * 软删除课程
     */
    int deleteCourse(@Param("id") Long id);
}
