package com.example.labmanagementsystembackend.mapper;

import com.example.labmanagementsystembackend.domain.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseMapper {
    Course findById(@Param("id") Long id);

    List<Course> findCourses(@Param("createdBy") Long createdBy,
                             @Param("term") String term,
                             @Param("offset") int offset,
                             @Param("pageSize") int pageSize);

    long countCourses(@Param("createdBy") Long createdBy, @Param("term") String term);

    /**
     * 查询学生选修的课程
     */
    @Select("SELECT c.* FROM courses c " +
            "INNER JOIN course_students cs ON c.id = cs.course_id " +
            "WHERE cs.student_id = #{studentId} " +
            "AND c.is_deleted = 0 " +
            "AND (#{term} IS NULL OR c.term = #{term}) " +
            "LIMIT #{offset}, #{pageSize}")
    List<Course> findCoursesByStudentId(@Param("studentId") Long studentId,
                                        @Param("term") String term,
                                        @Param("offset") int offset,
                                        @Param("pageSize") int pageSize);

    /**
     * 统计学生选修的课程数量
     */
    @Select("SELECT COUNT(*) FROM courses c " +
            "INNER JOIN course_students cs ON c.id = cs.course_id " +
            "WHERE cs.student_id = #{studentId} " +
            "AND c.is_deleted = 0 " +
            "AND (#{term} IS NULL OR c.term = #{term})")
    long countCoursesByStudentId(@Param("studentId") Long studentId, @Param("term") String term);

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
