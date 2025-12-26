package com.example.labmanagementsystembackend.controller;

import com.example.labmanagementsystembackend.common.api.ApiResponse;
import com.example.labmanagementsystembackend.common.api.PageResponse;
import com.example.labmanagementsystembackend.common.util.SecurityUtil;
import com.example.labmanagementsystembackend.dto.request.CourseCreateRequest;
import com.example.labmanagementsystembackend.dto.response.CourseResponse;
import com.example.labmanagementsystembackend.service.CourseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController extends BaseController {
    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ApiResponse<PageResponse<CourseResponse>> listCourses(@RequestParam(required = false) Long createdBy,
                                                                 @RequestParam(required = false) String term,
                                                                 @RequestParam(defaultValue = "1") int page,
                                                                 @RequestParam(defaultValue = "20") int pageSize,
                                                                 HttpServletRequest request) {
        List<CourseResponse> courses = courseService.listCourses(createdBy, term, page, pageSize);
        long total = courseService.countCourses(createdBy, term);
        return success(request, new PageResponse<>(courses, page, pageSize, total));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<CourseResponse> createCourse(@AuthenticationPrincipal Jwt jwt,
                                                    @Valid @RequestBody CourseCreateRequest body,
                                                    HttpServletRequest request) {
        Long createdBy = SecurityUtil.getUserId(jwt);
        return success(request, courseService.createCourse(createdBy, body));
    }

    @GetMapping("/{id}")
    public ApiResponse<CourseResponse> getCourse(@PathVariable Long id, HttpServletRequest request) {
        return success(request, courseService.getCourse(id));
    }
}
