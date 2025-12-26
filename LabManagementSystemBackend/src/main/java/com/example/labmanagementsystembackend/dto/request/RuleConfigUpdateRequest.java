package com.example.labmanagementsystembackend.dto.request;

import jakarta.validation.constraints.NotNull;

public class RuleConfigUpdateRequest {
    @NotNull
    private Object value;
    private String description;

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
