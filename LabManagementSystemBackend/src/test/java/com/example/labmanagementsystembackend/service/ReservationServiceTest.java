package com.example.labmanagementsystembackend.service;

import com.example.labmanagementsystembackend.common.error.BusinessException;
import com.example.labmanagementsystembackend.common.error.ErrorCode;
import com.example.labmanagementsystembackend.domain.entity.Device;
import com.example.labmanagementsystembackend.domain.entity.Lab;
import com.example.labmanagementsystembackend.domain.entity.Reservation;
import com.example.labmanagementsystembackend.dto.request.ReservationCreateRequest;
import com.example.labmanagementsystembackend.dto.response.ReservationCreateResponse;
import com.example.labmanagementsystembackend.mapper.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationMapper reservationMapper;
    @Mock
    private ReservationSeriesMapper reservationSeriesMapper;
    @Mock
    private ReservationSeriesItemMapper reservationSeriesItemMapper;
    @Mock
    private ApprovalMapper approvalMapper;
    @Mock
    private LabService labService;
    @Mock
    private DeviceService deviceService;
    @Mock
    private CourseService courseService;
    @Mock
    private UserService userService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private AuditLogService auditLogService;

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(
                reservationMapper,
                reservationSeriesMapper,
                reservationSeriesItemMapper,
                approvalMapper,
                labService,
                deviceService,
                courseService,
                userService,
                notificationService,
                auditLogService,
                new ObjectMapper()
        );
    }

    @Test
    void createReservation_conflict_throws() {
        Lab lab = buildLab("IDLE", "08:00:00", "22:00:00");
        when(labService.getLabEntity(1L)).thenReturn(lab);
        when(reservationMapper.findConflicts(eq(1L), isNull(), any(), any(), isNull()))
                .thenReturn(List.of(new Reservation()));

        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setLabId(1L);
        request.setStartTime(OffsetDateTime.of(2025, 1, 15, 9, 0, 0, 0, ZoneOffset.UTC));
        request.setEndTime(OffsetDateTime.of(2025, 1, 15, 10, 0, 0, 0, ZoneOffset.UTC));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> reservationService.createReservation(2L, request));
        assertEquals(ErrorCode.RESOURCE_CONFLICT, ex.getErrorCode());
    }

    @Test
    void createReservation_invalid_time_range() {
        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setLabId(1L);
        request.setStartTime(OffsetDateTime.of(2025, 1, 15, 10, 0, 0, 0, ZoneOffset.UTC));
        request.setEndTime(OffsetDateTime.of(2025, 1, 15, 9, 0, 0, 0, ZoneOffset.UTC));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> reservationService.createReservation(2L, request));
        assertEquals(ErrorCode.VALIDATION_FAILED, ex.getErrorCode());
    }

    @Test
    void createReservation_outside_open_hours() {
        Lab lab = buildLab("IDLE", "08:00:00", "22:00:00");
        when(labService.getLabEntity(1L)).thenReturn(lab);

        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setLabId(1L);
        request.setStartTime(OffsetDateTime.of(2025, 1, 15, 7, 0, 0, 0, ZoneOffset.UTC));
        request.setEndTime(OffsetDateTime.of(2025, 1, 15, 8, 0, 0, 0, ZoneOffset.UTC));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> reservationService.createReservation(2L, request));
        assertEquals(ErrorCode.RESERVATION_WINDOW_INVALID, ex.getErrorCode());
    }

    @Test
    void createReservation_device_sets_pending() {
        Lab lab = buildLab("IDLE", "08:00:00", "22:00:00");
        Device device = new Device();
        device.setId(9L);
        device.setStatus("IDLE");
        when(labService.getLabEntity(1L)).thenReturn(lab);
        when(deviceService.getDeviceEntity(9L)).thenReturn(device);
        when(reservationMapper.findConflicts(anyLong(), anyLong(), any(), any(), isNull()))
                .thenReturn(List.of());
        doAnswer(invocation -> {
            Reservation reservation = invocation.getArgument(0);
            reservation.setId(101L);
            return 1;
        }).when(reservationMapper).insertReservation(any(Reservation.class));

        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setLabId(1L);
        request.setDeviceId(9L);
        request.setStartTime(OffsetDateTime.of(2025, 1, 15, 9, 0, 0, 0, ZoneOffset.UTC));
        request.setEndTime(OffsetDateTime.of(2025, 1, 15, 10, 0, 0, 0, ZoneOffset.UTC));

        ReservationCreateResponse response = reservationService.createReservation(2L, request);
        assertEquals("PENDING", response.getStatus());
        assertEquals(101L, response.getReservationId());
    }

    private Lab buildLab(String status, String openStart, String openEnd) {
        Lab lab = new Lab();
        lab.setStatus(status);
        lab.setOpenTimeStart(java.time.LocalTime.parse(openStart));
        lab.setOpenTimeEnd(java.time.LocalTime.parse(openEnd));
        return lab;
    }
}
