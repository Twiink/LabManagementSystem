package com.example.labmanagementsystembackend.scheduler;

import com.example.labmanagementsystembackend.domain.entity.Reservation;
import com.example.labmanagementsystembackend.mapper.ReservationMapper;
import com.example.labmanagementsystembackend.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * 预约定时任务调度器
 *
 * 功能：
 * 1. 自动标记过期预约（APPROVED 状态但已过开始时间未签到）
 * 2. 自动完成超时预约（IN_USE 状态但已过结束时间未签出）
 */
@Component
public class ReservationScheduler {
    private static final Logger log = LoggerFactory.getLogger(ReservationScheduler.class);

    // 预约开始后多少分钟未签到视为过期
    private static final int EXPIRE_MINUTES_AFTER_START = 30;

    // 预约结束后多少分钟自动完成
    private static final int AUTO_COMPLETE_MINUTES_AFTER_END = 60;

    private final ReservationMapper reservationMapper;
    private final NotificationService notificationService;

    public ReservationScheduler(ReservationMapper reservationMapper,
                                NotificationService notificationService) {
        this.reservationMapper = reservationMapper;
        this.notificationService = notificationService;
    }

    /**
     * 每5分钟检查一次过期预约
     * 规则：APPROVED 状态的预约，开始时间超过30分钟未签到，标记为 EXPIRED
     */
    @Scheduled(fixedRate = 5 * 60 * 1000) // 每5分钟执行
    @Transactional
    public void expireUnusedReservations() {
        LocalDateTime threshold = LocalDateTime.now(ZoneOffset.UTC).minusMinutes(EXPIRE_MINUTES_AFTER_START);
        List<Reservation> expiredReservations = reservationMapper.findExpiredReservations(threshold);

        for (Reservation reservation : expiredReservations) {
            reservation.setStatus("EXPIRED");
            reservation.setCancelReason("Auto-expired: no check-in within " + EXPIRE_MINUTES_AFTER_START + " minutes");
            reservationMapper.updateReservationStatus(reservation);

            // 通知预约人
            notificationService.notifyUser(reservation.getRequesterId(), "RESERVATION",
                "Your reservation has expired due to no check-in");

            log.info("Reservation {} auto-expired", reservation.getId());
        }

        if (!expiredReservations.isEmpty()) {
            log.info("Expired {} reservations", expiredReservations.size());
        }
    }

    /**
     * 每10分钟检查一次需要自动完成的预约
     * 规则：IN_USE 状态的预约，结束时间超过60分钟未签出，自动标记为 COMPLETED
     */
    @Scheduled(fixedRate = 10 * 60 * 1000) // 每10分钟执行
    @Transactional
    public void autoCompleteOverdueReservations() {
        LocalDateTime threshold = LocalDateTime.now(ZoneOffset.UTC).minusMinutes(AUTO_COMPLETE_MINUTES_AFTER_END);
        List<Reservation> overdueReservations = reservationMapper.findOverdueReservations(threshold);

        for (Reservation reservation : overdueReservations) {
            reservation.setStatus("COMPLETED");
            reservationMapper.updateReservationStatus(reservation);

            // 通知预约人
            notificationService.notifyUser(reservation.getRequesterId(), "RESERVATION",
                "Your reservation has been auto-completed");

            log.info("Reservation {} auto-completed", reservation.getId());
        }

        if (!overdueReservations.isEmpty()) {
            log.info("Auto-completed {} overdue reservations", overdueReservations.size());
        }
    }

    /**
     * 每天凌晨2点清理长期未处理的 PENDING 状态预约
     * 规则：PENDING 状态超过7天的预约，标记为 EXPIRED
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    @Transactional
    public void expireOldPendingReservations() {
        LocalDateTime threshold = LocalDateTime.now(ZoneOffset.UTC).minusDays(7);
        List<Reservation> oldPendingReservations = reservationMapper.findOldPendingReservations(threshold);

        for (Reservation reservation : oldPendingReservations) {
            reservation.setStatus("EXPIRED");
            reservation.setCancelReason("Auto-expired: pending approval for more than 7 days");
            reservationMapper.updateReservationStatus(reservation);

            // 通知预约人
            notificationService.notifyUser(reservation.getRequesterId(), "RESERVATION",
                "Your reservation has expired due to long pending approval");

            log.info("Old pending reservation {} auto-expired", reservation.getId());
        }

        if (!oldPendingReservations.isEmpty()) {
            log.info("Expired {} old pending reservations", oldPendingReservations.size());
        }
    }
}
