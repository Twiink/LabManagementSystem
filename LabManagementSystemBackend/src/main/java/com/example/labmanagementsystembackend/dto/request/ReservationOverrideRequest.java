package com.example.labmanagementsystembackend.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ReservationOverrideRequest {
    @NotBlank
    private String reason;
    @NotBlank
    private String action;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
