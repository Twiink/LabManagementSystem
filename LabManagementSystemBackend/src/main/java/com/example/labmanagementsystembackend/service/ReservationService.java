package com.example.labmanagementsystembackend.service;

import com.example.labmanagementsystembackend.common.error.BusinessException;
import com.example.labmanagementsystembackend.common.error.ErrorCode;
import com.example.labmanagementsystembackend.common.util.PageUtil;
import com.example.labmanagementsystembackend.common.util.TimeUtil;
import com.example.labmanagementsystembackend.domain.entity.*;
import com.example.labmanagementsystembackend.dto.request.CourseReservationRequest;
import com.example.labmanagementsystembackend.dto.request.ReservationCreateRequest;
import com.example.labmanagementsystembackend.dto.request.ReservationSeriesRequest;
import com.example.labmanagementsystembackend.dto.request.ReservationUpdateRequest;
import com.example.labmanagementsystembackend.dto.response.CalendarItemResponse;
import com.example.labmanagementsystembackend.dto.response.ConflictDetail;
import com.example.labmanagementsystembackend.dto.response.ReservationCreateResponse;
import com.example.labmanagementsystembackend.dto.response.ReservationResponse;
import com.example.labmanagementsystembackend.dto.response.ReservationSeriesResponse;
import com.example.labmanagementsystembackend.mapper.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReservationService {
    private static final long TEACHER_CANCEL_HOURS = 12;
    private static final long STUDENT_CANCEL_HOURS = 24;

    private final ReservationMapper reservationMapper;
    private final ReservationSeriesMapper reservationSeriesMapper;
    private final ReservationSeriesItemMapper reservationSeriesItemMapper;
    private final ApprovalMapper approvalMapper;
    private final LabService labService;
    private final DeviceService deviceService;
    private final CourseService courseService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public ReservationService(ReservationMapper reservationMapper,
                              ReservationSeriesMapper reservationSeriesMapper,
                              ReservationSeriesItemMapper reservationSeriesItemMapper,
                              ApprovalMapper approvalMapper,
                              LabService labService,
                              DeviceService deviceService,
                              CourseService courseService,
                              UserService userService,
                              NotificationService notificationService,
                              AuditLogService auditLogService,
                              ObjectMapper objectMapper) {
        this.reservationMapper = reservationMapper;
        this.reservationSeriesMapper = reservationSeriesMapper;
        this.reservationSeriesItemMapper = reservationSeriesItemMapper;
        this.approvalMapper = approvalMapper;
        this.labService = labService;
        this.deviceService = deviceService;
        this.courseService = courseService;
        this.userService = userService;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    public List<ReservationResponse> listReservations(Long labId, Long deviceId, Long requesterId, List<String> status,
                                                      String type, OffsetDateTime from, OffsetDateTime to,
                                                      int page, int pageSize) {
        int safePage = PageUtil.normalizePage(page);
        int safeSize = PageUtil.normalizePageSize(pageSize);
        LocalDateTime fromTime = TimeUtil.toUtcLocalDateTime(from);
        LocalDateTime toTime = TimeUtil.toUtcLocalDateTime(to);
        return reservationMapper.findReservations(labId, deviceId, requesterId, status, type, fromTime, toTime,
                PageUtil.offset(safePage, safeSize), safeSize)
                .stream()
                .map(ReservationService::toResponse)
                .collect(Collectors.toList());
    }

    public long countReservations(Long labId, Long deviceId, Long requesterId, List<String> status,
                                  String type, OffsetDateTime from, OffsetDateTime to) {
        LocalDateTime fromTime = TimeUtil.toUtcLocalDateTime(from);
        LocalDateTime toTime = TimeUtil.toUtcLocalDateTime(to);
        return reservationMapper.countReservations(labId, deviceId, requesterId, status, type, fromTime, toTime);
    }

    public ReservationResponse getReservation(Long id) {
        Reservation reservation = reservationMapper.findById(id);
        if (reservation == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Reservation not found");
        }
        return toResponse(reservation);
    }

    @Transactional
    public ReservationCreateResponse createReservation(Long requesterId, ReservationCreateRequest request) {
        LocalDateTime startTime = TimeUtil.toUtcLocalDateTime(request.getStartTime());
        LocalDateTime endTime = TimeUtil.toUtcLocalDateTime(request.getEndTime());
        validateTimeRange(startTime, endTime);
        Lab lab = validateLab(request.getLabId());
        Device device = validateDevice(request.getDeviceId());
        validateOpenTime(lab, startTime, endTime);
        ensureNoConflicts(request.getLabId(), request.getDeviceId(), startTime, endTime, null);

        Reservation reservation = new Reservation();
        reservation.setRequesterId(requesterId);
        reservation.setLabId(request.getLabId());
        reservation.setDeviceId(request.getDeviceId());
        reservation.setTitle(request.getTitle());
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        reservation.setType("SINGLE");
        reservation.setPriority("NORMAL");
        // 所有预约都需要审批
        reservation.setStatus("PENDING");
        reservationMapper.insertReservation(reservation);

        auditLogService.record(requesterId, "RESERVATION_CREATE", "RESERVATION", reservation.getId(), null);
        notificationService.notifyUser(requesterId, "RESERVATION", "Reservation submitted, status: " + reservation.getStatus());
        return new ReservationCreateResponse(reservation.getId(), reservation.getStatus());
    }

    @Transactional
    public ReservationSeriesResponse createSeries(Long requesterId, ReservationSeriesRequest request) {
        List<TimeSlot> slots = generateSlots(request);
        ReservationSeries series = new ReservationSeries();
        series.setRequesterId(requesterId);
        series.setRuleType(request.getRule().getType());
        series.setRuleValue(toJson(request.getRule().getValue()));
        series.setMode(request.getRule().getMode());
        reservationSeriesMapper.insertSeries(series);

        List<Long> created = new ArrayList<>();
        List<ReservationSeriesResponse.FailedSlot> failed = new ArrayList<>();

        for (TimeSlot slot : slots) {
            try {
                ReservationCreateRequest createRequest = new ReservationCreateRequest();
                createRequest.setLabId(request.getLabId());
                createRequest.setDeviceId(request.getDeviceId());
                createRequest.setStartTime(slot.startTime);
                createRequest.setEndTime(slot.endTime);
                ReservationCreateResponse response = createReservation(requesterId, createRequest);
                created.add(response.getReservationId());
                ReservationSeriesItem item = new ReservationSeriesItem();
                item.setSeriesId(series.getId());
                item.setReservationId(response.getReservationId());
                item.setStatus(response.getStatus());
                reservationSeriesItemMapper.insertItem(item);
            } catch (BusinessException ex) {
                failed.add(new ReservationSeriesResponse.FailedSlot(slot.startTime, slot.endTime, ex.getMessage()));
                if ("STRICT".equalsIgnoreCase(series.getMode())) {
                    throw ex;
                }
            }
        }

        auditLogService.record(requesterId, "RESERVATION_SERIES_CREATE", "RESERVATION_SERIES", series.getId(), null);
        return new ReservationSeriesResponse(series.getId(), created, failed);
    }

    @Transactional
    public ReservationCreateResponse createCourseReservation(Long requesterId, CourseReservationRequest request) {
        courseService.getCourseEntity(request.getCourseId());
        LocalDateTime startTime = TimeUtil.toUtcLocalDateTime(request.getStartTime());
        LocalDateTime endTime = TimeUtil.toUtcLocalDateTime(request.getEndTime());
        validateTimeRange(startTime, endTime);
        Lab lab = validateLab(request.getLabId());
        Device device = validateDevice(request.getDeviceId());
        validateOpenTime(lab, startTime, endTime);
        ensureNoConflicts(request.getLabId(), request.getDeviceId(), startTime, endTime, null);

        Reservation reservation = new Reservation();
        reservation.setRequesterId(requesterId);
        reservation.setLabId(request.getLabId());
        reservation.setDeviceId(request.getDeviceId());
        reservation.setCourseId(request.getCourseId());
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        reservation.setType("COURSE");
        reservation.setPriority("COURSE");
        reservation.setStatus("PENDING");
        reservationMapper.insertReservation(reservation);

        auditLogService.record(requesterId, "RESERVATION_COURSE_CREATE", "RESERVATION", reservation.getId(), null);
        notificationService.notifyUser(requesterId, "RESERVATION", "Course reservation submitted, awaiting approval");
        return new ReservationCreateResponse(reservation.getId(), reservation.getStatus());
    }

    @Transactional
    public ReservationResponse updateReservation(Long requesterId, Long reservationId, ReservationUpdateRequest request) {
        Reservation reservation = getReservationEntity(reservationId);
        validateModifyPermission(requesterId, reservation);

        LocalDateTime startTime = TimeUtil.toUtcLocalDateTime(request.getStartTime());
        LocalDateTime endTime = TimeUtil.toUtcLocalDateTime(request.getEndTime());
        validateTimeRange(startTime, endTime);
        Lab lab = validateLab(reservation.getLabId());
        validateOpenTime(lab, startTime, endTime);
        ensureNoConflicts(reservation.getLabId(), reservation.getDeviceId(), startTime, endTime, reservation.getId());

        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        reservationMapper.updateReservationTime(reservation);
        auditLogService.record(requesterId, "RESERVATION_UPDATE", "RESERVATION", reservation.getId(), request.getReason());
        return toResponse(getReservationEntity(reservationId));
    }

    @Transactional
    public void cancelReservation(Long requesterId, Long reservationId, String reason, boolean isAdmin) {
        Reservation reservation = getReservationEntity(reservationId);
        validateCancelPermission(requesterId, reservation, isAdmin);
        reservation.setStatus("CANCELLED");
        reservation.setCancelReason(reason);
        reservationMapper.updateReservationStatus(reservation);
        auditLogService.record(requesterId, "RESERVATION_CANCEL", "RESERVATION", reservation.getId(), reason);
        notificationService.notifyUser(reservation.getRequesterId(), "RESERVATION", "Reservation cancelled");
    }

    /**
     * 预约签到（开始使用）
     * 权限：预约人本人或管理员可操作
     */
    @Transactional
    public void checkin(Long operatorId, Long reservationId) {
        Reservation reservation = getReservationEntity(reservationId);
        // 权限校验：只有预约人或管理员可以签到
        validateOperationPermission(operatorId, reservation, "check-in");
        if (!"APPROVED".equals(reservation.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATE, "Reservation state does not allow check-in");
        }
        reservation.setStatus("IN_USE");
        reservationMapper.updateReservationStatus(reservation);
        auditLogService.record(operatorId, "RESERVATION_CHECKIN", "RESERVATION", reservation.getId(), null);
    }

    /**
     * 预约签出（结束使用）
     * 权限：预约人本人或管理员可操作
     */
    @Transactional
    public void checkout(Long operatorId, Long reservationId) {
        Reservation reservation = getReservationEntity(reservationId);
        // 权限校验：只有预约人或管理员可以签出
        validateOperationPermission(operatorId, reservation, "check-out");
        if (!"IN_USE".equals(reservation.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATE, "Reservation state does not allow check-out");
        }
        reservation.setStatus("COMPLETED");
        reservationMapper.updateReservationStatus(reservation);
        auditLogService.record(operatorId, "RESERVATION_CHECKOUT", "RESERVATION", reservation.getId(), null);
    }

    @Transactional
    public void approve(Long operatorId, Long reservationId, String reason) {
        Reservation reservation = getReservationEntity(reservationId);
        if (!"PENDING".equals(reservation.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATE, "Reservation is not pending approval");
        }
        ensureNoConflicts(reservation.getLabId(), reservation.getDeviceId(), reservation.getStartTime(), reservation.getEndTime(), reservationId);
        reservation.setStatus("APPROVED");
        reservation.setApprovedBy(operatorId);
        reservation.setApprovedAt(LocalDateTime.now(ZoneOffset.UTC));
        reservationMapper.updateReservationStatus(reservation);

        Approval approval = new Approval();
        approval.setReservationId(reservationId);
        approval.setAction("APPROVE");
        approval.setReason(reason);
        approval.setOperatorId(operatorId);
        approvalMapper.insertApproval(approval);

        auditLogService.record(operatorId, "RESERVATION_APPROVE", "RESERVATION", reservationId, reason);
        notificationService.notifyUser(reservation.getRequesterId(), "RESERVATION", "Reservation approved");
    }

    @Transactional
    public void reject(Long operatorId, Long reservationId, String reason) {
        Reservation reservation = getReservationEntity(reservationId);
        if (!"PENDING".equals(reservation.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATE, "Reservation is not pending approval");
        }
        reservation.setStatus("REJECTED");
        reservation.setApprovedBy(operatorId);
        reservation.setApprovedAt(LocalDateTime.now(ZoneOffset.UTC));
        reservationMapper.updateReservationStatus(reservation);

        Approval approval = new Approval();
        approval.setReservationId(reservationId);
        approval.setAction("REJECT");
        approval.setReason(reason);
        approval.setOperatorId(operatorId);
        approvalMapper.insertApproval(approval);

        auditLogService.record(operatorId, "RESERVATION_REJECT", "RESERVATION", reservationId, reason);
        notificationService.notifyUser(reservation.getRequesterId(), "RESERVATION", "Reservation rejected");
    }

    @Transactional
    public void override(Long operatorId, Long reservationId, String reason, String action) {
        Reservation reservation = getReservationEntity(reservationId);
        List<Reservation> conflicts = reservationMapper.findConflicts(reservation.getLabId(), reservation.getDeviceId(),
                reservation.getStartTime(), reservation.getEndTime(), reservationId);
        if (!conflicts.isEmpty() && !"CANCEL_CONFLICTS".equalsIgnoreCase(action)) {
            throw new BusinessException(ErrorCode.RESOURCE_CONFLICT, "Conflicts exist, override policy required");
        }
        if ("CANCEL_CONFLICTS".equalsIgnoreCase(action)) {
            for (Reservation conflict : conflicts) {
                conflict.setStatus("CANCELLED");
                conflict.setCancelReason("Cancelled due to course override");
                reservationMapper.updateReservationStatus(conflict);
                notificationService.notifyUser(conflict.getRequesterId(), "RESERVATION", "Reservation cancelled by course override");
            }
        }
        reservation.setStatus("APPROVED");
        reservation.setApprovedBy(operatorId);
        reservation.setApprovedAt(LocalDateTime.now(ZoneOffset.UTC));
        reservationMapper.updateReservationStatus(reservation);

        Approval approval = new Approval();
        approval.setReservationId(reservationId);
        approval.setAction("OVERRIDE");
        approval.setReason(reason);
        approval.setOperatorId(operatorId);
        approvalMapper.insertApproval(approval);

        auditLogService.record(operatorId, "RESERVATION_OVERRIDE", "RESERVATION", reservationId, reason);
    }

    public List<CalendarItemResponse> calendar(Long labId, Long deviceId, OffsetDateTime from, OffsetDateTime to) {
        LocalDateTime fromTime = TimeUtil.toUtcLocalDateTime(from);
        LocalDateTime toTime = TimeUtil.toUtcLocalDateTime(to);
        return reservationMapper.findCalendar(labId, deviceId, fromTime, toTime)
                .stream()
                .map(reservation -> new CalendarItemResponse(reservation.getId(),
                        reservation.getType(),
                        TimeUtil.fromUtcLocalDateTime(reservation.getStartTime()),
                        TimeUtil.fromUtcLocalDateTime(reservation.getEndTime()),
                        reservation.getStatus(),
                        reservation.getType()))
                .collect(Collectors.toList());
    }

    private Reservation getReservationEntity(Long id) {
        Reservation reservation = reservationMapper.findById(id);
        if (reservation == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Reservation not found");
        }
        return reservation;
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Start time must be before end time");
        }
    }

    private Lab validateLab(Long labId) {
        Lab lab = labService.getLabEntity(labId);
        if ("MAINTENANCE".equals(lab.getStatus()) || "DISABLED".equals(lab.getStatus())) {
            throw new BusinessException(ErrorCode.RESOURCE_UNAVAILABLE, "Lab is unavailable for reservation");
        }
        return lab;
    }

    private Device validateDevice(Long deviceId) {
        if (deviceId == null) {
            return null;
        }
        Device device = deviceService.getDeviceEntity(deviceId);
        if ("MAINTENANCE".equals(device.getStatus()) || "RETIRED".equals(device.getStatus())) {
            throw new BusinessException(ErrorCode.RESOURCE_UNAVAILABLE, "Device is unavailable for reservation");
        }
        return device;
    }

    private void validateOpenTime(Lab lab, LocalDateTime startTime, LocalDateTime endTime) {
        if (lab.getOpenTimeStart() != null && lab.getOpenTimeEnd() != null) {
            if (startTime.toLocalTime().isBefore(lab.getOpenTimeStart())
                    || endTime.toLocalTime().isAfter(lab.getOpenTimeEnd())) {
                throw new BusinessException(ErrorCode.RESERVATION_WINDOW_INVALID, "Outside lab open hours");
            }
        }
    }

    private void ensureNoConflicts(Long labId, Long deviceId, LocalDateTime startTime, LocalDateTime endTime, Long excludeId) {
        List<Reservation> conflicts = reservationMapper.findConflicts(labId, deviceId, startTime, endTime, excludeId);
        if (!conflicts.isEmpty()) {
            List<ConflictDetail> details = conflicts.stream()
                    .map(conflict -> new ConflictDetail(conflict.getId(),
                            TimeUtil.fromUtcLocalDateTime(conflict.getStartTime()),
                            TimeUtil.fromUtcLocalDateTime(conflict.getEndTime())))
                    .collect(Collectors.toList());
            throw new BusinessException(ErrorCode.RESOURCE_CONFLICT, "Resource conflict", Map.of("conflicts", details));
        }
    }

    private void validateModifyPermission(Long requesterId, Reservation reservation) {
        User requester = userService.getUserEntity(requesterId);
        if ("ADMIN".equals(requester.getRole())) {
            return;
        }
        if (!requesterId.equals(reservation.getRequesterId())) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "Only reservation owner can modify");
        }
        long hours = Duration.between(OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime(), reservation.getStartTime()).toHours();
        if ("TEACHER".equals(requester.getRole()) && hours < TEACHER_CANCEL_HOURS) {
            throw new BusinessException(ErrorCode.RESERVATION_WINDOW_INVALID, "Teachers must modify at least 12 hours in advance");
        }
        if ("STUDENT".equals(requester.getRole()) && hours < STUDENT_CANCEL_HOURS) {
            throw new BusinessException(ErrorCode.RESERVATION_WINDOW_INVALID, "Students must modify at least 24 hours in advance");
        }
    }

    private void validateCancelPermission(Long requesterId, Reservation reservation, boolean isAdmin) {
        if (isAdmin) {
            return;
        }
        User requester = userService.getUserEntity(requesterId);
        if (!requesterId.equals(reservation.getRequesterId())) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "Only reservation owner can cancel");
        }
        long hours = Duration.between(OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime(), reservation.getStartTime()).toHours();
        if ("TEACHER".equals(requester.getRole()) && hours < TEACHER_CANCEL_HOURS) {
            throw new BusinessException(ErrorCode.RESERVATION_WINDOW_INVALID, "Teachers must cancel at least 12 hours in advance");
        }
        if ("STUDENT".equals(requester.getRole()) && hours < STUDENT_CANCEL_HOURS) {
            throw new BusinessException(ErrorCode.RESERVATION_WINDOW_INVALID, "Students must cancel at least 24 hours in advance");
        }
    }

    /**
     * 验证操作权限（签到/签出等）
     * 规则：预约人本人或管理员可操作
     */
    private void validateOperationPermission(Long operatorId, Reservation reservation, String operation) {
        User operator = userService.getUserEntity(operatorId);
        // 管理员可以操作任何预约
        if ("ADMIN".equals(operator.getRole())) {
            return;
        }
        // 非管理员只能操作自己的预约
        if (!operatorId.equals(reservation.getRequesterId())) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED,
                "Only reservation owner or admin can " + operation);
        }
    }

    private List<TimeSlot> generateSlots(ReservationSeriesRequest request) {
        String type = request.getRule().getType();
        OffsetDateTime start = request.getTime().getStartTime();
        OffsetDateTime end = request.getTime().getEndTime();
        if (start == null || end == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Time range is required");
        }
        if (!start.isBefore(end)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Start time must be before end time");
        }
        if ("DAILY".equalsIgnoreCase(type)) {
            Map<String, Object> value = toMap(request.getRule().getValue());
            int count = ((Number) value.getOrDefault("count", 1)).intValue();
            int interval = ((Number) value.getOrDefault("interval", 1)).intValue();
            return buildDailySlots(start, end, count, interval);
        }
        if ("WEEKLY".equalsIgnoreCase(type)) {
            Map<String, Object> value = toMap(request.getRule().getValue());
            @SuppressWarnings("unchecked")
            List<Integer> days = (List<Integer>) value.getOrDefault("daysOfWeek", List.of());
            int count = ((Number) value.getOrDefault("count", days.size())).intValue();
            return buildWeeklySlots(start, end, days, count);
        }
        if ("CUSTOM".equalsIgnoreCase(type)) {
            Map<String, Object> value = toMap(request.getRule().getValue());
            @SuppressWarnings("unchecked")
            List<String> dates = (List<String>) value.getOrDefault("dates", List.of());
            return buildCustomSlots(start, end, dates);
        }
        throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Unsupported recurrence rule type");
    }

    private List<TimeSlot> buildDailySlots(OffsetDateTime start, OffsetDateTime end, int count, int interval) {
        List<TimeSlot> slots = new ArrayList<>();
        OffsetDateTime currentStart = start;
        OffsetDateTime currentEnd = end;
        for (int i = 0; i < count; i++) {
            slots.add(new TimeSlot(currentStart, currentEnd));
            currentStart = currentStart.plusDays(interval);
            currentEnd = currentEnd.plusDays(interval);
        }
        return slots;
    }

    private List<TimeSlot> buildWeeklySlots(OffsetDateTime start, OffsetDateTime end, List<Integer> daysOfWeek, int count) {
        List<TimeSlot> slots = new ArrayList<>();
        if (daysOfWeek == null || daysOfWeek.isEmpty()) {
            return slots;
        }
        Set<DayOfWeek> targetDays = daysOfWeek.stream()
                .map(day -> DayOfWeek.of(Math.min(Math.max(day, 1), 7)))
                .collect(Collectors.toSet());
        LocalDate date = start.toLocalDate();
        while (slots.size() < count) {
            if (targetDays.contains(date.getDayOfWeek())) {
                OffsetDateTime slotStart = date.atTime(start.toLocalTime()).atOffset(start.getOffset());
                OffsetDateTime slotEnd = date.atTime(end.toLocalTime()).atOffset(end.getOffset());
                slots.add(new TimeSlot(slotStart, slotEnd));
            }
            date = date.plusDays(1);
        }
        return slots;
    }

    private List<TimeSlot> buildCustomSlots(OffsetDateTime start, OffsetDateTime end, List<String> dates) {
        List<TimeSlot> slots = new ArrayList<>();
        for (String dateStr : dates) {
            LocalDate date = LocalDate.parse(dateStr);
            OffsetDateTime slotStart = date.atTime(start.toLocalTime()).atOffset(start.getOffset());
            OffsetDateTime slotEnd = date.atTime(end.toLocalTime()).atOffset(end.getOffset());
            slots.add(new TimeSlot(slotStart, slotEnd));
        }
        return slots;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Rule config cannot be parsed");
        }
    }

    private Map<String, Object> toMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new HashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                result.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            return result;
        }
        return objectMapper.convertValue(value, Map.class);
    }

    private static ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(reservation.getId(), reservation.getRequesterId(), reservation.getLabId(),
                reservation.getDeviceId(), reservation.getCourseId(), reservation.getTitle(),
                TimeUtil.fromUtcLocalDateTime(reservation.getStartTime()),
                TimeUtil.fromUtcLocalDateTime(reservation.getEndTime()),
                reservation.getStatus(), reservation.getType(), reservation.getPriority());
    }

    private record TimeSlot(OffsetDateTime startTime, OffsetDateTime endTime) {
    }
}
