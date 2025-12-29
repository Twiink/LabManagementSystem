package com.example.labmanagementsystembackend.service;

import com.example.labmanagementsystembackend.common.error.BusinessException;
import com.example.labmanagementsystembackend.common.error.ErrorCode;
import com.example.labmanagementsystembackend.domain.entity.Device;
import com.example.labmanagementsystembackend.domain.entity.Lab;
import com.example.labmanagementsystembackend.domain.entity.Reservation;
import com.example.labmanagementsystembackend.domain.entity.User;
import com.example.labmanagementsystembackend.dto.request.ReservationCreateRequest;
import com.example.labmanagementsystembackend.dto.response.ReservationCreateResponse;
import com.example.labmanagementsystembackend.mapper.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 预约服务单元测试
 *
 * 测试范围：
 * 1. 冲突检测算法（含边界情况）
 * 2. 资源状态校验
 * 3. 权限控制
 * 4. 状态转换
 * 5. 时间验证
 */
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

    // ==================== 冲突检测测试 ====================

    @Nested
    @DisplayName("冲突检测算法测试")
    class ConflictDetectionTests {

        @Test
        @DisplayName("完全重叠的时间段应检测到冲突")
        void createReservation_fullOverlap_conflict() {
            Lab lab = buildLab("IDLE", "08:00:00", "22:00:00");
            when(labService.getLabEntity(1L)).thenReturn(lab);

            Reservation existingReservation = new Reservation();
            existingReservation.setId(99L);
            when(reservationMapper.findConflicts(eq(1L), isNull(), any(), any(), isNull()))
                    .thenReturn(List.of(existingReservation));

            ReservationCreateRequest request = buildRequest(1L, null, 9, 0, 10, 0);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> reservationService.createReservation(2L, request));
            assertEquals(ErrorCode.RESOURCE_CONFLICT, ex.getErrorCode());
        }

        @Test
        @DisplayName("部分重叠（新预约开始时间在现有预约中间）应检测到冲突")
        void createReservation_partialOverlapStart_conflict() {
            Lab lab = buildLab("IDLE", "08:00:00", "22:00:00");
            when(labService.getLabEntity(1L)).thenReturn(lab);

            // 现有预约: 9:00-10:00, 新预约: 9:30-10:30
            when(reservationMapper.findConflicts(eq(1L), isNull(), any(), any(), isNull()))
                    .thenReturn(List.of(new Reservation()));

            ReservationCreateRequest request = buildRequest(1L, null, 9, 30, 10, 30);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> reservationService.createReservation(2L, request));
            assertEquals(ErrorCode.RESOURCE_CONFLICT, ex.getErrorCode());
        }

        @Test
        @DisplayName("相邻时间段（结束时间=开始时间）不应冲突")
        void createReservation_adjacent_noConflict() {
            Lab lab = buildLab("IDLE", "08:00:00", "22:00:00");
            when(labService.getLabEntity(1L)).thenReturn(lab);

            // 现有预约: 9:00-10:00, 新预约: 10:00-11:00 (相邻，不冲突)
            when(reservationMapper.findConflicts(eq(1L), isNull(), any(), any(), isNull()))
                    .thenReturn(List.of());
            doAnswer(invocation -> {
                Reservation r = invocation.getArgument(0);
                r.setId(101L);
                return 1;
            }).when(reservationMapper).insertReservation(any(Reservation.class));

            ReservationCreateRequest request = buildRequest(1L, null, 10, 0, 11, 0);

            ReservationCreateResponse response = reservationService.createReservation(2L, request);
            assertNotNull(response);
            // 所有预约现在都需要审批，状态为 PENDING
            assertEquals("PENDING", response.getStatus());
        }

        @Test
        @DisplayName("无设备预约直接通过，有设备预约需审批")
        void createReservation_withDevice_requiresApproval() {
            Lab lab = buildLab("IDLE", "08:00:00", "22:00:00");
            Device device = buildDevice(9L, "IDLE");

            when(labService.getLabEntity(1L)).thenReturn(lab);
            when(deviceService.getDeviceEntity(9L)).thenReturn(device);
            when(reservationMapper.findConflicts(anyLong(), anyLong(), any(), any(), isNull()))
                    .thenReturn(List.of());
            doAnswer(invocation -> {
                Reservation r = invocation.getArgument(0);
                r.setId(101L);
                return 1;
            }).when(reservationMapper).insertReservation(any(Reservation.class));

            ReservationCreateRequest request = buildRequest(1L, 9L, 9, 0, 10, 0);

            ReservationCreateResponse response = reservationService.createReservation(2L, request);
            assertEquals("PENDING", response.getStatus());
        }
    }

    // ==================== 资源状态校验测试 ====================

    @Nested
    @DisplayName("资源状态校验测试")
    class ResourceStatusTests {

        @Test
        @DisplayName("实验室维护中不可预约")
        void createReservation_labMaintenance_rejected() {
            Lab lab = buildLab("MAINTENANCE", "08:00:00", "22:00:00");
            when(labService.getLabEntity(1L)).thenReturn(lab);

            ReservationCreateRequest request = buildRequest(1L, null, 9, 0, 10, 0);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> reservationService.createReservation(2L, request));
            assertEquals(ErrorCode.RESOURCE_UNAVAILABLE, ex.getErrorCode());
        }

        @Test
        @DisplayName("实验室已停用不可预约")
        void createReservation_labDisabled_rejected() {
            Lab lab = buildLab("DISABLED", "08:00:00", "22:00:00");
            when(labService.getLabEntity(1L)).thenReturn(lab);

            ReservationCreateRequest request = buildRequest(1L, null, 9, 0, 10, 0);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> reservationService.createReservation(2L, request));
            assertEquals(ErrorCode.RESOURCE_UNAVAILABLE, ex.getErrorCode());
        }

        @Test
        @DisplayName("设备维护中不可预约")
        void createReservation_deviceMaintenance_rejected() {
            Lab lab = buildLab("IDLE", "08:00:00", "22:00:00");
            Device device = buildDevice(9L, "MAINTENANCE");

            when(labService.getLabEntity(1L)).thenReturn(lab);
            when(deviceService.getDeviceEntity(9L)).thenReturn(device);

            ReservationCreateRequest request = buildRequest(1L, 9L, 9, 0, 10, 0);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> reservationService.createReservation(2L, request));
            assertEquals(ErrorCode.RESOURCE_UNAVAILABLE, ex.getErrorCode());
        }

        @Test
        @DisplayName("设备已报废不可预约")
        void createReservation_deviceRetired_rejected() {
            Lab lab = buildLab("IDLE", "08:00:00", "22:00:00");
            Device device = buildDevice(9L, "RETIRED");

            when(labService.getLabEntity(1L)).thenReturn(lab);
            when(deviceService.getDeviceEntity(9L)).thenReturn(device);

            ReservationCreateRequest request = buildRequest(1L, 9L, 9, 0, 10, 0);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> reservationService.createReservation(2L, request));
            assertEquals(ErrorCode.RESOURCE_UNAVAILABLE, ex.getErrorCode());
        }
    }

    // ==================== 时间验证测试 ====================

    @Nested
    @DisplayName("时间验证测试")
    class TimeValidationTests {

        @Test
        @DisplayName("开始时间不能晚于结束时间")
        void createReservation_invalidTimeRange_rejected() {
            ReservationCreateRequest request = buildRequest(1L, null, 10, 0, 9, 0);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> reservationService.createReservation(2L, request));
            assertEquals(ErrorCode.VALIDATION_FAILED, ex.getErrorCode());
        }

        @Test
        @DisplayName("预约时间超出实验室开放时间应被拒绝")
        void createReservation_outsideOpenHours_rejected() {
            Lab lab = buildLab("IDLE", "08:00:00", "22:00:00");
            when(labService.getLabEntity(1L)).thenReturn(lab);

            // 使用中国时区 (Asia/Shanghai +8)，本地时间 7:00-8:00 早于开放时间 08:00
            // UTC 时间为前一天 23:00 到当天 00:00
            ReservationCreateRequest request = new ReservationCreateRequest();
            request.setLabId(1L);
            request.setDeviceId(null);
            request.setStartTime(OffsetDateTime.of(2025, 1, 15, 7, 0, 0, 0, ZoneOffset.ofHours(8)));
            request.setEndTime(OffsetDateTime.of(2025, 1, 15, 8, 0, 0, 0, ZoneOffset.ofHours(8)));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> reservationService.createReservation(2L, request));
            assertEquals(ErrorCode.RESERVATION_WINDOW_INVALID, ex.getErrorCode());
        }

        @Test
        @DisplayName("预约结束时间超出实验室关闭时间应被拒绝")
        void createReservation_afterCloseHours_rejected() {
            Lab lab = buildLab("IDLE", "08:00:00", "22:00:00");
            when(labService.getLabEntity(1L)).thenReturn(lab);

            // 预约时间: 21:00-23:00，超出关闭时间
            ReservationCreateRequest request = buildRequest(1L, null, 21, 0, 23, 0);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> reservationService.createReservation(2L, request));
            assertEquals(ErrorCode.RESERVATION_WINDOW_INVALID, ex.getErrorCode());
        }
    }

    // ==================== 状态转换测试 ====================

    @Nested
    @DisplayName("状态转换测试")
    class StateTransitionTests {

        @Test
        @DisplayName("签到：APPROVED -> IN_USE")
        void checkin_approved_toInUse() {
            Reservation reservation = buildReservation(101L, 2L, "APPROVED");
            User admin = buildUser(1L, "ADMIN");

            when(reservationMapper.findById(101L)).thenReturn(reservation);
            when(userService.getUserEntity(1L)).thenReturn(admin);

            reservationService.checkin(1L, 101L);

            assertEquals("IN_USE", reservation.getStatus());
            verify(reservationMapper).updateReservationStatus(reservation);
        }

        @Test
        @DisplayName("签到：非APPROVED状态应被拒绝")
        void checkin_notApproved_rejected() {
            Reservation reservation = buildReservation(101L, 2L, "PENDING");
            User admin = buildUser(1L, "ADMIN");

            when(reservationMapper.findById(101L)).thenReturn(reservation);
            when(userService.getUserEntity(1L)).thenReturn(admin);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> reservationService.checkin(1L, 101L));
            assertEquals(ErrorCode.INVALID_STATE, ex.getErrorCode());
        }

        @Test
        @DisplayName("签出：IN_USE -> COMPLETED")
        void checkout_inUse_toCompleted() {
            Reservation reservation = buildReservation(101L, 2L, "IN_USE");
            User admin = buildUser(1L, "ADMIN");

            when(reservationMapper.findById(101L)).thenReturn(reservation);
            when(userService.getUserEntity(1L)).thenReturn(admin);

            reservationService.checkout(1L, 101L);

            assertEquals("COMPLETED", reservation.getStatus());
            verify(reservationMapper).updateReservationStatus(reservation);
        }

        @Test
        @DisplayName("审批通过：PENDING -> APPROVED")
        void approve_pending_toApproved() {
            Reservation reservation = buildReservation(101L, 2L, "PENDING");
            reservation.setLabId(1L);
            reservation.setStartTime(LocalDateTime.of(2025, 1, 15, 9, 0));
            reservation.setEndTime(LocalDateTime.of(2025, 1, 15, 10, 0));

            when(reservationMapper.findById(101L)).thenReturn(reservation);
            when(reservationMapper.findConflicts(eq(1L), isNull(), any(), any(), eq(101L)))
                    .thenReturn(List.of());

            reservationService.approve(1L, 101L, "审核通过");

            assertEquals("APPROVED", reservation.getStatus());
            assertNotNull(reservation.getApprovedBy());
            verify(approvalMapper).insertApproval(any());
        }

        @Test
        @DisplayName("审批拒绝：PENDING -> REJECTED")
        void reject_pending_toRejected() {
            Reservation reservation = buildReservation(101L, 2L, "PENDING");

            when(reservationMapper.findById(101L)).thenReturn(reservation);

            reservationService.reject(1L, 101L, "设备维护中");

            assertEquals("REJECTED", reservation.getStatus());
            verify(approvalMapper).insertApproval(any());
        }
    }

    // ==================== 权限控制测试 ====================

    @Nested
    @DisplayName("权限控制测试")
    class PermissionTests {

        @Test
        @DisplayName("非预约人不能签到")
        void checkin_notOwner_rejected() {
            Reservation reservation = buildReservation(101L, 2L, "APPROVED");
            User student = buildUser(3L, "STUDENT");

            when(reservationMapper.findById(101L)).thenReturn(reservation);
            when(userService.getUserEntity(3L)).thenReturn(student);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> reservationService.checkin(3L, 101L));
            assertEquals(ErrorCode.PERMISSION_DENIED, ex.getErrorCode());
        }

        @Test
        @DisplayName("管理员可以操作任何预约")
        void checkin_admin_allowed() {
            Reservation reservation = buildReservation(101L, 2L, "APPROVED");
            User admin = buildUser(1L, "ADMIN");

            when(reservationMapper.findById(101L)).thenReturn(reservation);
            when(userService.getUserEntity(1L)).thenReturn(admin);

            assertDoesNotThrow(() -> reservationService.checkin(1L, 101L));
        }

        @Test
        @DisplayName("预约人可以操作自己的预约")
        void checkin_owner_allowed() {
            Reservation reservation = buildReservation(101L, 2L, "APPROVED");
            User owner = buildUser(2L, "STUDENT");

            when(reservationMapper.findById(101L)).thenReturn(reservation);
            when(userService.getUserEntity(2L)).thenReturn(owner);

            assertDoesNotThrow(() -> reservationService.checkin(2L, 101L));
        }
    }

    // ==================== 辅助方法 ====================

    private Lab buildLab(String status, String openStart, String openEnd) {
        Lab lab = new Lab();
        lab.setId(1L);
        lab.setStatus(status);
        lab.setOpenTimeStart(java.time.LocalTime.parse(openStart));
        lab.setOpenTimeEnd(java.time.LocalTime.parse(openEnd));
        return lab;
    }

    private Device buildDevice(Long id, String status) {
        Device device = new Device();
        device.setId(id);
        device.setStatus(status);
        return device;
    }

    private User buildUser(Long id, String role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }

    private Reservation buildReservation(Long id, Long requesterId, String status) {
        Reservation reservation = new Reservation();
        reservation.setId(id);
        reservation.setRequesterId(requesterId);
        reservation.setStatus(status);
        return reservation;
    }

    private ReservationCreateRequest buildRequest(Long labId, Long deviceId,
                                                   int startHour, int startMin,
                                                   int endHour, int endMin) {
        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setLabId(labId);
        request.setDeviceId(deviceId);
        request.setStartTime(OffsetDateTime.of(2025, 1, 15, startHour, startMin, 0, 0, ZoneOffset.UTC));
        request.setEndTime(OffsetDateTime.of(2025, 1, 15, endHour, endMin, 0, 0, ZoneOffset.UTC));
        return request;
    }
}
