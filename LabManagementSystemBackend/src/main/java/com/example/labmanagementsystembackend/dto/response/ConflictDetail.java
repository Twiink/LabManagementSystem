package com.example.labmanagementsystembackend.dto.response;

import java.time.OffsetDateTime;

public class ConflictDetail {
    private Long reservationId;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;

    public ConflictDetail(Long reservationId, OffsetDateTime startTime, OffsetDateTime endTime) {
        this.reservationId = reservationId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public OffsetDateTime getStartTime() {
        return startTime;
    }

    public OffsetDateTime getEndTime() {
        return endTime;
    }
}
