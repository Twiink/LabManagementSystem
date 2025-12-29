package com.example.labmanagementsystembackend.dto.response;

import java.time.OffsetDateTime;

public class CalendarItemResponse {
    private Long id;
    private String title;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private String status;
    private String type;
    private Long labId;
    private Long deviceId;
    private String requesterName;

    public CalendarItemResponse(Long id, String title, OffsetDateTime startTime, OffsetDateTime endTime, String status,
                                String type, Long labId, Long deviceId, String requesterName) {
        this.id = id;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.type = type;
        this.labId = labId;
        this.deviceId = deviceId;
        this.requesterName = requesterName;
    }

    public Long getId() {
        return id;
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

    public Long getLabId() {
        return labId;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public String getRequesterName() {
        return requesterName;
    }
}
