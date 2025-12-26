package com.example.labmanagementsystembackend.controller;

import com.example.labmanagementsystembackend.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;

public abstract class BaseController {
    protected <T> ApiResponse<T> success(HttpServletRequest request, T data) {
        String requestId = (String) request.getAttribute("requestId");
        return ApiResponse.success(data, requestId);
    }
}
