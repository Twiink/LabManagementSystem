package com.example.labmanagementsystembackend.service;

import com.example.labmanagementsystembackend.common.error.BusinessException;
import com.example.labmanagementsystembackend.common.error.ErrorCode;
import com.example.labmanagementsystembackend.common.util.PageUtil;
import com.example.labmanagementsystembackend.domain.entity.Course;
import com.example.labmanagementsystembackend.domain.entity.User;
import com.example.labmanagementsystembackend.dto.request.CourseCreateRequest;
import com.example.labmanagementsystembackend.dto.request.CourseUpdateRequest;
import com.example.labmanagementsystembackend.dto.response.CourseResponse;
import com.example.labmanagementsystembackend.mapper.CourseMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {
    private final CourseMapper courseMapper;
    private final UserService userService;

    public CourseService(CourseMapper courseMapper, UserService userService) {
        this.courseMapper = courseMapper;
        this.userService = userService;
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

    @Transactional
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

    /**
     * 更新课程信息
     *
     * @param operatorId 操作人ID
     * @param id         课程ID
     * @param request    更新请求
     * @return 更新后的课程信息
     */
    @Transactional
    public CourseResponse updateCourse(Long operatorId, Long id, CourseUpdateRequest request) {
        Course course = getCourseEntity(id);
        // 权限校验：只有创建者或管理员可以更新
        validateCoursePermission(operatorId, course, "update");

        course.setName(request.getName());
        course.setClassName(request.getClassName());
        course.setStudentCount(request.getStudentCount());
        course.setTerm(request.getTerm());
        courseMapper.updateCourse(course);

        return toResponse(courseMapper.findById(id));
    }

    /**
     * 删除课程
     *
     * @param operatorId 操作人ID
     * @param id         课程ID
     */
    @Transactional
    public void deleteCourse(Long operatorId, Long id) {
        Course course = getCourseEntity(id);
        // 权限校验：只有创建者或管理员可以删除
        validateCoursePermission(operatorId, course, "delete");

        courseMapper.deleteCourse(id);
    }

    public Course getCourseEntity(Long id) {
        Course course = courseMapper.findById(id);
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Course not found");
        }
        return course;
    }

    /**
     * 验证课程操作权限
     * 规则：创建者或管理员可以操作
     */
    private void validateCoursePermission(Long operatorId, Course course, String operation) {
        User operator = userService.getUserEntity(operatorId);
        // 管理员可以操作任何课程
        if ("ADMIN".equals(operator.getRole())) {
            return;
        }
        // 创建者可以操作自己的课程
        if (!operatorId.equals(course.getCreatedBy())) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED,
                    "Only course creator or admin can " + operation + " this course");
        }
    }

    private static CourseResponse toResponse(Course course) {
        return new CourseResponse(course.getId(), course.getName(), course.getClassName(), course.getStudentCount(), course.getTerm(), course.getCreatedBy());
    }
}
