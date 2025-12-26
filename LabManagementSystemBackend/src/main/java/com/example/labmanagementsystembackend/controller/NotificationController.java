package com.example.labmanagementsystembackend.controller;

import com.example.labmanagementsystembackend.common.api.ApiResponse;
import com.example.labmanagementsystembackend.common.api.PageResponse;
import com.example.labmanagementsystembackend.dto.response.NotificationResponse;
import com.example.labmanagementsystembackend.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController extends BaseController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<PageResponse<NotificationResponse>> listNotifications(@RequestParam(required = false) String status,
                                                                            @RequestParam(defaultValue = "1") int page,
                                                                            @RequestParam(defaultValue = "20") int pageSize,
                                                                            HttpServletRequest request) {
        List<NotificationResponse> items = notificationService.listNotifications(status, page, pageSize);
        long total = notificationService.countNotifications(status);
        return success(request, new PageResponse<>(items, page, pageSize, total));
    }

    @PostMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable Long id, HttpServletRequest request) {
        notificationService.markRead(id);
        return success(request, null);
    }
}
