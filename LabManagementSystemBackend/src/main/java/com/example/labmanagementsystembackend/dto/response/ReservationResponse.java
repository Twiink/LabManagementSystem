package com.example.labmanagementsystembackend.dto.response;

import java.time.OffsetDateTime;

public class ReservationResponse {
    private Long id;
    private Long requesterId;
    private Long labId;
    private Long deviceId;
    private Long courseId;
    private String title;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private String status;
    private String type;
    private String priority;

    public ReservationResponse(Long id, Long requesterId, Long labId, Long deviceId, Long courseId, String title,
                               OffsetDateTime startTime, OffsetDateTime endTime, String status, String type, String priority) {
        this.id = id;
        this.requesterId = requesterId;
        this.labId = labId;
        this.deviceId = deviceId;
        this.courseId = courseId;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.type = type;
        this.priority = priority;
    }

    public Long getId() {
        return id;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public Long getLabId() {
        return labId;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public Long getCourseId() {
        return courseId;
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

    public String getPriority() {
        return priority;
    }
}
