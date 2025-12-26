package com.example.labmanagementsystembackend.controller;

import com.example.labmanagementsystembackend.common.api.ApiResponse;
import com.example.labmanagementsystembackend.common.api.PageResponse;
import com.example.labmanagementsystembackend.dto.request.CourseCreateRequest;
import com.example.labmanagementsystembackend.dto.response.CourseResponse;
import com.example.labmanagementsystembackend.service.CourseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
    public ApiResponse<CourseResponse> createCourse(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                                    @Valid @RequestBody CourseCreateRequest body,
                                                    HttpServletRequest request) {
        Long createdBy = userId == null ? 1L : userId;
        return success(request, courseService.createCourse(createdBy, body));
    }

    @GetMapping("/{id}")
    public ApiResponse<CourseResponse> getCourse(@PathVariable Long id, HttpServletRequest request) {
        return success(request, courseService.getCourse(id));
    }
}
