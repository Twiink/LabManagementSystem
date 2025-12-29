package com.example.labmanagementsystembackend.controller;

import com.example.labmanagementsystembackend.common.api.ApiResponse;
import com.example.labmanagementsystembackend.common.api.PageResponse;
import com.example.labmanagementsystembackend.common.util.SecurityUtil;
import com.example.labmanagementsystembackend.dto.request.*;
import com.example.labmanagementsystembackend.dto.response.ReservationCreateResponse;
import com.example.labmanagementsystembackend.dto.response.ReservationResponse;
import com.example.labmanagementsystembackend.dto.response.ReservationSeriesResponse;
import com.example.labmanagementsystembackend.service.ReservationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController extends BaseController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ApiResponse<PageResponse<ReservationResponse>> listReservations(@RequestParam(required = false) Long labId,
                                                                           @RequestParam(required = false) Long deviceId,
                                                                           @RequestParam(required = false) Long requesterId,
                                                                           @RequestParam(required = false) String status,
                                                                           @RequestParam(required = false) String type,
                                                                           @RequestParam(required = false) OffsetDateTime from,
                                                                           @RequestParam(required = false) OffsetDateTime to,
                                                                           @RequestParam(defaultValue = "1") int page,
                                                                           @RequestParam(defaultValue = "20") int pageSize,
                                                                           HttpServletRequest request) {
        List<String> statusList = status == null ? null : Arrays.asList(status.split(","));
        List<ReservationResponse> reservations = reservationService.listReservations(labId, deviceId, requesterId,
                statusList, type, from, to, page, pageSize);
        long total = reservationService.countReservations(labId, deviceId, requesterId, statusList, type, from, to);
        return success(request, new PageResponse<>(reservations, page, pageSize, total));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER','STUDENT','ADMIN')")
    public ApiResponse<ReservationCreateResponse> createReservation(@AuthenticationPrincipal Jwt jwt,
                                                                    @Valid @RequestBody ReservationCreateRequest body,
                                                                    HttpServletRequest request) {
        Long requesterId = SecurityUtil.getUserId(jwt);
        return success(request, reservationService.createReservation(requesterId, body));
    }

    @PostMapping("/series")
    @PreAuthorize("hasAnyRole('TEACHER','STUDENT','ADMIN')")
    public ApiResponse<ReservationSeriesResponse> createSeries(@AuthenticationPrincipal Jwt jwt,
                                                               @Valid @RequestBody ReservationSeriesRequest body,
                                                               HttpServletRequest request) {
        Long requesterId = SecurityUtil.getUserId(jwt);
        return success(request, reservationService.createSeries(requesterId, body));
    }

    @PostMapping("/course")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<ReservationCreateResponse> createCourse(@AuthenticationPrincipal Jwt jwt,
                                                               @Valid @RequestBody CourseReservationRequest body,
                                                               HttpServletRequest request) {
        Long requesterId = SecurityUtil.getUserId(jwt);
        return success(request, reservationService.createCourseReservation(requesterId, body));
    }

    @GetMapping("/{id}")
    public ApiResponse<ReservationResponse> getReservation(@PathVariable Long id, HttpServletRequest request) {
        return success(request, reservationService.getReservation(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<ReservationResponse> updateReservation(@AuthenticationPrincipal Jwt jwt,
                                                              @PathVariable Long id,
                                                              @Valid @RequestBody ReservationUpdateRequest body,
                                                              HttpServletRequest request) {
        Long requesterId = SecurityUtil.getUserId(jwt);
        return success(request, reservationService.updateReservation(requesterId, id, body));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> cancelReservation(@AuthenticationPrincipal Jwt jwt,
                                               @PathVariable Long id,
                                               @RequestParam(required = false) String reason,
                                               HttpServletRequest request) {
        Long requesterId = SecurityUtil.getUserId(jwt);
        boolean isAdmin = "ADMIN".equals(SecurityUtil.getRole(jwt));
        reservationService.cancelReservation(requesterId, id, reason, isAdmin);
        return success(request, null);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ApiResponse<Void> approve(@AuthenticationPrincipal Jwt jwt,
                                     @PathVariable Long id,
                                     @RequestBody(required = false) ReservationApprovalRequest body,
                                     HttpServletRequest request) {
        Long operatorId = SecurityUtil.getUserId(jwt);
        String reason = body != null ? body.getReason() : null;
        reservationService.approve(operatorId, id, reason);
        return success(request, null);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ApiResponse<Void> reject(@AuthenticationPrincipal Jwt jwt,
                                    @PathVariable Long id,
                                    @RequestBody(required = false) ReservationApprovalRequest body,
                                    HttpServletRequest request) {
        Long operatorId = SecurityUtil.getUserId(jwt);
        String reason = body != null ? body.getReason() : null;
        reservationService.reject(operatorId, id, reason);
        return success(request, null);
    }

    @PostMapping("/{id}/override")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> override(@AuthenticationPrincipal Jwt jwt,
                                      @PathVariable Long id,
                                      @Valid @RequestBody ReservationOverrideRequest body,
                                      HttpServletRequest request) {
        Long operatorId = SecurityUtil.getUserId(jwt);
        reservationService.override(operatorId, id, body.getReason(), body.getAction());
        return success(request, null);
    }

    @PostMapping("/{id}/checkin")
    public ApiResponse<Void> checkin(@AuthenticationPrincipal Jwt jwt,
                                     @PathVariable Long id,
                                     HttpServletRequest request) {
        Long operatorId = SecurityUtil.getUserId(jwt);
        reservationService.checkin(operatorId, id);
        return success(request, null);
    }

    @PostMapping("/{id}/checkout")
    public ApiResponse<Void> checkout(@AuthenticationPrincipal Jwt jwt,
                                      @PathVariable Long id,
                                      HttpServletRequest request) {
        Long operatorId = SecurityUtil.getUserId(jwt);
        reservationService.checkout(operatorId, id);
        return success(request, null);
    }
}
