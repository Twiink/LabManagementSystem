package com.example.labmanagementsystembackend.service;

import com.example.labmanagementsystembackend.common.error.BusinessException;
import com.example.labmanagementsystembackend.common.error.ErrorCode;
import com.example.labmanagementsystembackend.common.util.PageUtil;
import com.example.labmanagementsystembackend.domain.entity.Course;
import com.example.labmanagementsystembackend.dto.request.CourseCreateRequest;
import com.example.labmanagementsystembackend.dto.response.CourseResponse;
import com.example.labmanagementsystembackend.mapper.CourseMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {
    private final CourseMapper courseMapper;

    public CourseService(CourseMapper courseMapper) {
        this.courseMapper = courseMapper;
    }

    public List<CourseResponse> listCourses(Long createdBy, String term, int page, int pageSize) {
        int safePage = PageUtil.normalizePage(page);
        int safeSize = PageUtil.normalizePageSize(pageSize);
        return courseMapper.findCourses(createdBy, term, PageUtil.offset(safePage, safeSize), safeSize)
                .stream()
                .map(CourseService::toResponse)
                .collect(Collectors.toList());
    }

    public long countCourses(Long createdBy, String term) {
        return courseMapper.countCourses(createdBy, term);
    }

    public CourseResponse createCourse(Long createdBy, CourseCreateRequest request) {
        Course course = new Course();
        course.setName(request.getName());
        course.setClassName(request.getClassName());
        course.setStudentCount(request.getStudentCount());
        course.setTerm(request.getTerm());
        course.setCreatedBy(createdBy);
        courseMapper.insertCourse(course);
        return toResponse(courseMapper.findById(course.getId()));
    }

    public CourseResponse getCourse(Long id) {
        Course course = courseMapper.findById(id);
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Course not found");
        }
        return toResponse(course);
    }

    public Course getCourseEntity(Long id) {
        Course course = courseMapper.findById(id);
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Course not found");
        }
        return course;
    }

    private static CourseResponse toResponse(Course course) {
        return new CourseResponse(course.getId(), course.getName(), course.getClassName(), course.getStudentCount(), course.getTerm(), course.getCreatedBy());
    }
}
