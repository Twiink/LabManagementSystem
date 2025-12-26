package com.example.labmanagementsystembackend.controller;

import com.example.labmanagementsystembackend.common.api.ApiResponse;
import com.example.labmanagementsystembackend.common.api.PageResponse;
import com.example.labmanagementsystembackend.dto.request.*;
import com.example.labmanagementsystembackend.dto.response.ReservationCreateResponse;
import com.example.labmanagementsystembackend.dto.response.ReservationResponse;
import com.example.labmanagementsystembackend.dto.response.ReservationSeriesResponse;
import com.example.labmanagementsystembackend.service.ReservationService;
import com.example.labmanagementsystembackend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController extends BaseController {
    private final ReservationService reservationService;
    private final UserService userService;

    public ReservationController(ReservationService reservationService, UserService userService) {
        this.reservationService = reservationService;
        this.userService = userService;
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
    public ApiResponse<ReservationCreateResponse> createReservation(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                                                    @Valid @RequestBody ReservationCreateRequest body,
                                                                    HttpServletRequest request) {
        Long requesterId = userId == null ? 1L : userId;
        return success(request, reservationService.createReservation(requesterId, body));
    }

    @PostMapping("/series")
    public ApiResponse<ReservationSeriesResponse> createSeries(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                                               @Valid @RequestBody ReservationSeriesRequest body,
                                                               HttpServletRequest request) {
        Long requesterId = userId == null ? 1L : userId;
        return success(request, reservationService.createSeries(requesterId, body));
    }

    @PostMapping("/course")
    public ApiResponse<ReservationCreateResponse> createCourse(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                                               @Valid @RequestBody CourseReservationRequest body,
                                                               HttpServletRequest request) {
        Long requesterId = userId == null ? 1L : userId;
        return success(request, reservationService.createCourseReservation(requesterId, body));
    }

    @GetMapping("/{id}")
    public ApiResponse<ReservationResponse> getReservation(@PathVariable Long id, HttpServletRequest request) {
        return success(request, reservationService.getReservation(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<ReservationResponse> updateReservation(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                                              @PathVariable Long id,
                                                              @Valid @RequestBody ReservationUpdateRequest body,
                                                              HttpServletRequest request) {
        Long requesterId = userId == null ? 1L : userId;
        return success(request, reservationService.updateReservation(requesterId, id, body));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> cancelReservation(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                               @PathVariable Long id,
                                               @RequestParam(required = false) String reason,
                                               HttpServletRequest request) {
        Long requesterId = userId == null ? 1L : userId;
        boolean isAdmin = "ADMIN".equals(userService.getUserEntity(requesterId).getRole());
        reservationService.cancelReservation(requesterId, id, reason, isAdmin);
        return success(request, null);
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<Void> approve(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                     @PathVariable Long id,
                                     @Valid @RequestBody ReservationApprovalRequest body,
                                     HttpServletRequest request) {
        Long operatorId = userId == null ? 1L : userId;
        reservationService.approve(operatorId, id, body.getReason());
        return success(request, null);
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<Void> reject(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                    @PathVariable Long id,
                                    @Valid @RequestBody ReservationApprovalRequest body,
                                    HttpServletRequest request) {
        Long operatorId = userId == null ? 1L : userId;
        reservationService.reject(operatorId, id, body.getReason());
        return success(request, null);
    }

    @PostMapping("/{id}/override")
    public ApiResponse<Void> override(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                      @PathVariable Long id,
                                      @Valid @RequestBody ReservationOverrideRequest body,
                                      HttpServletRequest request) {
        Long operatorId = userId == null ? 1L : userId;
        reservationService.override(operatorId, id, body.getReason(), body.getAction());
        return success(request, null);
    }

    @PostMapping("/{id}/checkin")
    public ApiResponse<Void> checkin(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                     @PathVariable Long id,
                                     HttpServletRequest request) {
        Long operatorId = userId == null ? 1L : userId;
        reservationService.checkin(operatorId, id);
        return success(request, null);
    }

    @PostMapping("/{id}/checkout")
    public ApiResponse<Void> checkout(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                      @PathVariable Long id,
                                      HttpServletRequest request) {
        Long operatorId = userId == null ? 1L : userId;
        reservationService.checkout(operatorId, id);
        return success(request, null);
    }
}
