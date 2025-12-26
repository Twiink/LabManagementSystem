package com.example.labmanagementsystembackend.dto.response;

import java.time.OffsetDateTime;

public class RuleConfigResponse {
    private Long id;
    private String keyName;
    private String value;
    private String description;
    private OffsetDateTime updatedAt;

    public RuleConfigResponse(Long id, String keyName, String value, String description, OffsetDateTime updatedAt) {
        this.id = id;
        this.keyName = keyName;
        this.value = value;
        this.description = description;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getKeyName() {
        return keyName;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
