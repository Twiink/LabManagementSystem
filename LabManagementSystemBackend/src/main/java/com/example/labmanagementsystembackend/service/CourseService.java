package com.example.labmanagementsystembackend.service;

import com.example.labmanagementsystembackend.common.error.BusinessException;
import com.example.labmanagementsystembackend.common.error.ErrorCode;
import com.example.labmanagementsystembackend.common.util.PageUtil;
import com.example.labmanagementsystembackend.domain.entity.Course;
import com.example.labmanagementsystembackend.domain.entity.CourseStudent;
import com.example.labmanagementsystembackend.domain.entity.Lab;
import com.example.labmanagementsystembackend.domain.entity.User;
import com.example.labmanagementsystembackend.dto.request.CourseCreateRequest;
import com.example.labmanagementsystembackend.dto.request.CourseUpdateRequest;
import com.example.labmanagementsystembackend.dto.response.CourseResponse;
import com.example.labmanagementsystembackend.mapper.CourseMapper;
import com.example.labmanagementsystembackend.mapper.CourseStudentMapper;
import com.example.labmanagementsystembackend.mapper.LabMapper;
import com.example.labmanagementsystembackend.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {
    private final CourseMapper courseMapper;
    private final CourseStudentMapper courseStudentMapper;
    private final UserService userService;
    private final UserMapper userMapper;
    private final LabMapper labMapper;
    private final AuditLogService auditLogService;

    public CourseService(CourseMapper courseMapper, CourseStudentMapper courseStudentMapper, UserService userService, UserMapper userMapper, LabMapper labMapper, AuditLogService auditLogService) {
        this.courseMapper = courseMapper;
        this.courseStudentMapper = courseStudentMapper;
        this.userService = userService;
        this.userMapper = userMapper;
        this.labMapper = labMapper;
        this.auditLogService = auditLogService;
    }

    public List<CourseResponse> listCourses(Long createdBy, String term, int page, int pageSize) {
        int safePage = PageUtil.normalizePage(page);
        int safeSize = PageUtil.normalizePageSize(pageSize);
        return courseMapper.findCourses(createdBy, term, PageUtil.offset(safePage, safeSize), safeSize)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public long countCourses(Long createdBy, String term) {
        return courseMapper.countCourses(createdBy, term);
    }

    /**
     * 查询学生选修的课程列表
     */
    public List<CourseResponse> listCoursesByStudentId(Long studentId, String term, int page, int pageSize) {
        int safePage = PageUtil.normalizePage(page);
        int safeSize = PageUtil.normalizePageSize(pageSize);
        return courseMapper.findCoursesByStudentId(studentId, term, PageUtil.offset(safePage, safeSize), safeSize)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 统计学生选修的课程数量
     */
    public long countCoursesByStudentId(Long studentId, String term) {
        return courseMapper.countCoursesByStudentId(studentId, term);
    }

    @Transactional
    public CourseResponse createCourse(Long createdBy, CourseCreateRequest request) {
        Course course = new Course();
        course.setName(request.getName());
        course.setClassName(request.getClassName());
        course.setStudentCount(request.getStudentIds() != null ? request.getStudentIds().size() : 0);
        course.setTerm(request.getTerm());
        course.setLabId(request.getLabId());
        course.setScheduleTime(request.getScheduleTime());
        course.setCreatedBy(createdBy);
        courseMapper.insertCourse(course);

        // 创建课程-学生关联
        if (request.getStudentIds() != null && !request.getStudentIds().isEmpty()) {
            for (Long studentId : request.getStudentIds()) {
                CourseStudent courseStudent = new CourseStudent();
                courseStudent.setCourseId(course.getId());
                courseStudent.setStudentId(studentId);
                courseStudentMapper.insertCourseStudent(courseStudent);
            }
        }

        // 记录审计日志
        String detail = String.format("{\"name\":\"%s\",\"term\":\"%s\",\"studentCount\":%d}",
                course.getName(), course.getTerm(), course.getStudentCount());
        auditLogService.record(createdBy, "CREATE", "COURSE", course.getId(), detail);

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

        String oldName = course.getName();
        course.setName(request.getName());
        course.setClassName(request.getClassName());
        course.setStudentCount(request.getStudentCount());
        course.setTerm(request.getTerm());
        course.setLabId(request.getLabId());
        course.setScheduleTime(request.getScheduleTime());
        courseMapper.updateCourse(course);

        // 记录审计日志
        String detail = String.format("{\"oldName\":\"%s\",\"newName\":\"%s\",\"term\":\"%s\"}",
                oldName, request.getName(), request.getTerm());
        auditLogService.record(operatorId, "UPDATE", "COURSE", id, detail);

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

        String courseName = course.getName();

        // 删除课程-学生关联
        courseStudentMapper.deleteByCourseId(id);
        // 删除课程
        courseMapper.deleteCourse(id);

        // 记录审计日志
        String detail = String.format("{\"name\":\"%s\",\"term\":\"%s\"}", courseName, course.getTerm());
        auditLogService.record(operatorId, "DELETE", "COURSE", id, detail);
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

    private CourseResponse toResponse(Course course) {
        CourseResponse response = new CourseResponse(
                course.getId(),
                course.getName(),
                course.getClassName(),
                course.getStudentCount(),
                course.getTerm(),
                course.getCreatedBy()
        );

        // 填充实验室信息
        if (course.getLabId() != null) {
            Lab lab = labMapper.findById(course.getLabId());
            if (lab != null) {
                response.setLabId(course.getLabId());
                response.setLabName(lab.getName());
            }
        }

        // 填充上课时间
        response.setScheduleTime(course.getScheduleTime());

        // 填充教师名称
        if (course.getCreatedBy() != null) {
            User teacher = userMapper.findById(course.getCreatedBy());
            if (teacher != null) {
                response.setTeacherName(teacher.getName());
            }
        }

        // 填充学生IDs
        List<Long> studentIds = courseStudentMapper.findStudentIdsByCourseId(course.getId());
        response.setStudentIds(studentIds);

        return response;
    }
}
