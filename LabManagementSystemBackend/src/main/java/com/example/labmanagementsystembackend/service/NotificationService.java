package com.example.labmanagementsystembackend.service;

import com.example.labmanagementsystembackend.common.error.BusinessException;
import com.example.labmanagementsystembackend.common.error.ErrorCode;
import com.example.labmanagementsystembackend.common.util.PageUtil;
import com.example.labmanagementsystembackend.common.util.TimeUtil;
import com.example.labmanagementsystembackend.domain.entity.Notification;
import com.example.labmanagementsystembackend.dto.response.NotificationResponse;
import com.example.labmanagementsystembackend.mapper.NotificationMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    private final NotificationMapper notificationMapper;

    public NotificationService(NotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    public void notifyUser(Long userId, String type, String content) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setContent(content);
        notification.setStatus("UNREAD");
        notificationMapper.insertNotification(notification);
    }

    public List<NotificationResponse> listNotifications(String status, int page, int pageSize) {
        int safePage = PageUtil.normalizePage(page);
        int safeSize = PageUtil.normalizePageSize(pageSize);
        return notificationMapper.findNotifications(status, PageUtil.offset(safePage, safeSize), safeSize)
                .stream()
                .map(NotificationService::toResponse)
                .collect(Collectors.toList());
    }

    public long countNotifications(String status) {
        return notificationMapper.countNotifications(status);
    }

    public void markRead(Long id) {
        Notification notification = notificationMapper.findById(id);
        if (notification == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Notification not found");
        }
        notificationMapper.markRead(id);
    }

    private static NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(notification.getId(), notification.getUserId(), notification.getType(),
                notification.getContent(), notification.getStatus(), TimeUtil.fromUtcLocalDateTime(notification.getCreatedAt()));
    }
}
