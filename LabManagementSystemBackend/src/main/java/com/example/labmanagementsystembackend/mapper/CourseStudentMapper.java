package com.example.labmanagementsystembackend.mapper;

import com.example.labmanagementsystembackend.domain.entity.CourseStudent;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CourseStudentMapper {
    @Insert("INSERT INTO course_students(course_id, student_id, created_at) " +
            "VALUES(#{courseId}, #{studentId}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertCourseStudent(CourseStudent courseStudent);

    @Select("SELECT * FROM course_students WHERE course_id = #{courseId}")
    List<CourseStudent> findByCourseId(Long courseId);

    @Select("SELECT * FROM course_students WHERE student_id = #{studentId}")
    List<CourseStudent> findByStudentId(Long studentId);

    @Delete("DELETE FROM course_students WHERE course_id = #{courseId}")
    void deleteByCourseId(Long courseId);

    @Delete("DELETE FROM course_students WHERE course_id = #{courseId} AND student_id = #{studentId}")
    void deleteCourseStudent(@Param("courseId") Long courseId, @Param("studentId") Long studentId);

    @Select("SELECT student_id FROM course_students WHERE course_id = #{courseId}")
    List<Long> findStudentIdsByCourseId(Long courseId);
}
