package com.example.labmanagementsystembackend.dto.response;

import java.time.OffsetDateTime;

public class NotificationResponse {
    private Long id;
    private Long userId;
    private String type;
    private String content;
    private String status;
    private OffsetDateTime createdAt;

    public NotificationResponse(Long id, Long userId, String type, String content, String status, OffsetDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.content = content;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public String getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
