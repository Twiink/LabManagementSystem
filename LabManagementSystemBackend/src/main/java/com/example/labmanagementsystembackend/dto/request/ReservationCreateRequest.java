package com.example.labmanagementsystembackend.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public class ReservationCreateRequest {
    @NotNull
    private Long labId;
    private Long deviceId;
    private String title;
    @NotNull
    private OffsetDateTime startTime;
    @NotNull
    private OffsetDateTime endTime;
    private String reason;

    public Long getLabId() {
        return labId;
    }

    public void setLabId(Long labId) {
        this.labId = labId;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public OffsetDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(OffsetDateTime startTime) {
        this.startTime = startTime;
    }

    public OffsetDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(OffsetDateTime endTime) {
        this.endTime = endTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
