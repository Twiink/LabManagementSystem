package com.example.labmanagementsystembackend.controller;

import com.example.labmanagementsystembackend.common.api.ApiResponse;
import com.example.labmanagementsystembackend.dto.response.CalendarItemResponse;
import com.example.labmanagementsystembackend.service.ReservationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController extends BaseController {
    private final ReservationService reservationService;

    public CalendarController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ApiResponse<List<CalendarItemResponse>> calendar(@RequestParam(required = false) Long labId,
                                                            @RequestParam(required = false) Long deviceId,
                                                            @RequestParam OffsetDateTime from,
                                                            @RequestParam OffsetDateTime to,
                                                            HttpServletRequest request) {
        return success(request, reservationService.calendar(labId, deviceId, from, to));
    }
}
