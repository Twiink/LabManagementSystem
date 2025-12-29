package com.example.labmanagementsystembackend.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public class ReservationSeriesRequest {
    @NotNull
    private Long labId;
    private Long deviceId;
    private String title;
    @NotNull
    private Rule rule;
    @NotNull
    private TimeRange time;

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

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public TimeRange getTime() {
        return time;
    }

    public void setTime(TimeRange time) {
        this.time = time;
    }

    public static class Rule {
        @NotNull
        private String type;
        @NotNull
        private Object value;
        @NotNull
        private String mode;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }
    }

    public static class TimeRange {
        @NotNull
        private OffsetDateTime startTime;
        @NotNull
        private OffsetDateTime endTime;

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
    }
}
