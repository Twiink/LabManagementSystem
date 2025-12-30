package com.example.labmanagementsystembackend.controller;

import com.example.labmanagementsystembackend.common.api.ApiResponse;
import com.example.labmanagementsystembackend.common.api.PageResponse;
import com.example.labmanagementsystembackend.common.util.SecurityUtil;
import com.example.labmanagementsystembackend.dto.request.DeviceCreateRequest;
import com.example.labmanagementsystembackend.dto.request.DeviceStatusUpdateRequest;
import com.example.labmanagementsystembackend.dto.request.DeviceUpdateRequest;
import com.example.labmanagementsystembackend.dto.response.DeviceResponse;
import com.example.labmanagementsystembackend.service.DeviceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController extends BaseController {
    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public ApiResponse<PageResponse<DeviceResponse>> listDevices(@RequestParam(required = false) Long labId,
                                                                 @RequestParam(required = false) String status,
                                                                 @RequestParam(required = false) String keyword,
                                                                 @RequestParam(defaultValue = "1") int page,
                                                                 @RequestParam(defaultValue = "20") int pageSize,
                                                                 HttpServletRequest request) {
        List<DeviceResponse> devices = deviceService.listDevices(labId, status, keyword, page, pageSize);
        long total = deviceService.countDevices(labId, status, keyword);
        return success(request, new PageResponse<>(devices, page, pageSize, total));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DeviceResponse> createDevice(@AuthenticationPrincipal Jwt jwt,
                                                    @Valid @RequestBody DeviceCreateRequest body,
                                                    HttpServletRequest request) {
        Long actorId = SecurityUtil.getUserId(jwt);
        return success(request, deviceService.createDevice(actorId, body));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DeviceResponse> updateDevice(@AuthenticationPrincipal Jwt jwt,
                                                    @PathVariable Long id,
                                                    @Valid @RequestBody DeviceUpdateRequest body,
                                                    HttpServletRequest request) {
        Long actorId = SecurityUtil.getUserId(jwt);
        return success(request, deviceService.updateDevice(actorId, id, body));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateStatus(@AuthenticationPrincipal Jwt jwt,
                                          @PathVariable Long id,
                                          @Valid @RequestBody DeviceStatusUpdateRequest body,
                                          HttpServletRequest request) {
        Long actorId = SecurityUtil.getUserId(jwt);
        deviceService.updateStatus(actorId, id, body.getStatus());
        return success(request, null);
    }
}
