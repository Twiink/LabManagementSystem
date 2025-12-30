package com.example.labmanagementsystembackend.service;

import com.example.labmanagementsystembackend.common.util.PageUtil;
import com.example.labmanagementsystembackend.common.util.TimeUtil;
import com.example.labmanagementsystembackend.domain.entity.AuditLog;
import com.example.labmanagementsystembackend.dto.response.AuditLogResponse;
import com.example.labmanagementsystembackend.mapper.AuditLogMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditLogService {
    private final AuditLogMapper auditLogMapper;

    public AuditLogService(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    public void record(Long actorId, String action, String targetType, Long targetId, String detail) {
        AuditLog log = new AuditLog();
        log.setActorId(actorId);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetail(detail);
        log.setCreatedAt(LocalDateTime.now());
        log.setUpdatedAt(LocalDateTime.now());
        auditLogMapper.insertLog(log);
    }

    public List<AuditLogResponse> listLogs(Long actorId, String targetType, Long targetId,
                                           OffsetDateTime fromTime, OffsetDateTime toTime,
                                           int page, int pageSize) {
        int safePage = PageUtil.normalizePage(page);
        int safeSize = PageUtil.normalizePageSize(pageSize);
        LocalDateTime from = TimeUtil.toUtcLocalDateTime(fromTime);
        LocalDateTime to = TimeUtil.toUtcLocalDateTime(toTime);
        return auditLogMapper.findLogs(actorId, targetType, targetId, from, to, PageUtil.offset(safePage, safeSize), safeSize)
                .stream()
                .map(AuditLogService::toResponse)
                .collect(Collectors.toList());
    }

    public long countLogs(Long actorId, String targetType, Long targetId, OffsetDateTime fromTime, OffsetDateTime toTime) {
        LocalDateTime from = TimeUtil.toUtcLocalDateTime(fromTime);
        LocalDateTime to = TimeUtil.toUtcLocalDateTime(toTime);
        return auditLogMapper.countLogs(actorId, targetType, targetId, from, to);
    }

    private static AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogResponse(log.getId(), log.getActorId(), log.getActorName(), log.getAction(), log.getTargetType(), log.getTargetId(),
                log.getDetail(), TimeUtil.fromUtcLocalDateTime(log.getCreatedAt()));
    }
}
