package com.example.labmanagementsystembackend.dto.response;

import java.time.OffsetDateTime;

public class CalendarItemResponse {
    private Long reservationId;
    private String title;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private String status;
    private String type;

    public CalendarItemResponse(Long reservationId, String title, OffsetDateTime startTime, OffsetDateTime endTime, String status, String type) {
        this.reservationId = reservationId;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.type = type;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public String getTitle() {
        return title;
    }

    public OffsetDateTime getStartTime() {
        return startTime;
    }

    public OffsetDateTime getEndTime() {
        return endTime;
    }

    public String getStatus() {
        return status;
    }

    public String getType() {
        return type;
    }
}
