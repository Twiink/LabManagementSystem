package com.example.labmanagementsystembackend.controller;

import com.example.labmanagementsystembackend.common.api.ApiResponse;
import com.example.labmanagementsystembackend.common.api.PageResponse;
import com.example.labmanagementsystembackend.common.util.SecurityUtil;
import com.example.labmanagementsystembackend.dto.request.CourseCreateRequest;
import com.example.labmanagementsystembackend.dto.request.CourseUpdateRequest;
import com.example.labmanagementsystembackend.dto.response.CourseResponse;
import com.example.labmanagementsystembackend.service.CourseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程管理控制器
 *
 * 接口路径: /api/courses
 * 功能: 课程的增删改查
 * 权限: 查询公开，创建/更新/删除需要 TEACHER 或 ADMIN 权限
 */
@RestController
@RequestMapping("/api/courses")
public class CourseController extends BaseController {
    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * 获取课程列表
     *
     * @param createdBy 按创建人筛选（教师查看自己创建的课程）
     * @param studentId 按学生ID筛选（学生查看自己选修的课程）
     * @param term 按学期筛选
     */
    @GetMapping
    public ApiResponse<PageResponse<CourseResponse>> listCourses(@RequestParam(required = false) Long createdBy,
                                                                 @RequestParam(required = false) Long studentId,
                                                                 @RequestParam(required = false) String term,
                                                                 @RequestParam(defaultValue = "1") int page,
                                                                 @RequestParam(defaultValue = "20") int pageSize,
                                                                 HttpServletRequest request) {
        List<CourseResponse> courses;
        long total;

        if (studentId != null) {
            // 查询学生选修的课程
            courses = courseService.listCoursesByStudentId(studentId, term, page, pageSize);
            total = courseService.countCoursesByStudentId(studentId, term);
        } else {
            // 查询教师创建的课程或所有课程
            courses = courseService.listCourses(createdBy, term, page, pageSize);
            total = courseService.countCourses(createdBy, term);
        }

        return success(request, new PageResponse<>(courses, page, pageSize, total));
    }

    /**
     * 创建课程
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<CourseResponse> createCourse(@AuthenticationPrincipal Jwt jwt,
                                                    @Valid @RequestBody CourseCreateRequest body,
                                                    HttpServletRequest request) {
        Long createdBy = SecurityUtil.getUserId(jwt);
        return success(request, courseService.createCourse(createdBy, body));
    }

    /**
     * 获取课程详情
     */
    @GetMapping("/{id}")
    public ApiResponse<CourseResponse> getCourse(@PathVariable Long id, HttpServletRequest request) {
        return success(request, courseService.getCourse(id));
    }

    /**
     * 更新课程信息
     * 权限: 课程创建者或管理员
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<CourseResponse> updateCourse(@AuthenticationPrincipal Jwt jwt,
                                                    @PathVariable Long id,
                                                    @Valid @RequestBody CourseUpdateRequest body,
                                                    HttpServletRequest request) {
        Long operatorId = SecurityUtil.getUserId(jwt);
        return success(request, courseService.updateCourse(operatorId, id, body));
    }

    /**
     * 删除课程
     * 权限: 课程创建者或管理员
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<Void> deleteCourse(@AuthenticationPrincipal Jwt jwt,
                                          @PathVariable Long id,
                                          HttpServletRequest request) {
        Long operatorId = SecurityUtil.getUserId(jwt);
        courseService.deleteCourse(operatorId, id);
        return success(request, null);
    }
}
