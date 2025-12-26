package com.example.labmanagementsystembackend.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ReservationApprovalRequest {
    @NotBlank
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
