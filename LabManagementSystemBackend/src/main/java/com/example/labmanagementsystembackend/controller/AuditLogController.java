package com.example.labmanagementsystembackend.controller;

import com.example.labmanagementsystembackend.common.api.ApiResponse;
import com.example.labmanagementsystembackend.common.api.PageResponse;
import com.example.labmanagementsystembackend.dto.response.AuditLogResponse;
import com.example.labmanagementsystembackend.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController extends BaseController {
    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<AuditLogResponse>> listLogs(@RequestParam(required = false) Long actorId,
                                                                @RequestParam(required = false) String targetType,
                                                                @RequestParam(required = false) Long targetId,
                                                                @RequestParam(required = false) OffsetDateTime from,
                                                                @RequestParam(required = false) OffsetDateTime to,
                                                                @RequestParam(defaultValue = "1") int page,
                                                                @RequestParam(defaultValue = "20") int pageSize,
                                                                HttpServletRequest request) {
        List<AuditLogResponse> items = auditLogService.listLogs(actorId, targetType, targetId, from, to, page, pageSize);
        long total = auditLogService.countLogs(actorId, targetType, targetId, from, to);
        return success(request, new PageResponse<>(items, page, pageSize, total));
    }
}
