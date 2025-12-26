package com.example.labmanagementsystembackend.controller;

import com.example.labmanagementsystembackend.common.api.ApiResponse;
import com.example.labmanagementsystembackend.common.api.PageResponse;
import com.example.labmanagementsystembackend.dto.response.UserResponse;
import com.example.labmanagementsystembackend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController extends BaseController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> currentUser(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                                 HttpServletRequest request) {
        Long resolvedId = userId == null ? 1L : userId;
        return success(request, userService.getUserById(resolvedId));
    }

    @GetMapping
    public ApiResponse<PageResponse<UserResponse>> listUsers(@RequestParam(required = false) String role,
                                                             @RequestParam(required = false) String status,
                                                             @RequestParam(defaultValue = "1") int page,
                                                             @RequestParam(defaultValue = "20") int pageSize,
                                                             HttpServletRequest request) {
        List<UserResponse> users = userService.listUsers(role, status, page, pageSize);
        long total = userService.countUsers(role, status);
        return success(request, new PageResponse<>(users, page, pageSize, total));
    }
}
