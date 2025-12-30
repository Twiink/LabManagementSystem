package com.example.labmanagementsystembackend.dto.response;

import java.time.OffsetDateTime;

public class AuditLogResponse {
    private Long id;
    private Long actorId;
    private String actorName;
    private String action;
    private String targetType;
    private Long targetId;
    private String detail;
    private OffsetDateTime createdAt;

    public AuditLogResponse(Long id, Long actorId, String actorName, String action, String targetType, Long targetId, String detail, OffsetDateTime createdAt) {
        this.id = id;
        this.actorId = actorId;
        this.actorName = actorName;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.detail = detail;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getActorId() {
        return actorId;
    }

    public String getActorName() {
        return actorName;
    }

    public String getAction() {
        return action;
    }

    public String getTargetType() {
        return targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public String getDetail() {
        return detail;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
