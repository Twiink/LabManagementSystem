package com.example.labmanagementsystembackend.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

public class ReservationSeriesResponse {
    private Long seriesId;
    private List<ReservationResponse> created;
    private List<FailedSlot> failed;

    public ReservationSeriesResponse(Long seriesId, List<ReservationResponse> created, List<FailedSlot> failed) {
        this.seriesId = seriesId;
        this.created = created;
        this.failed = failed;
    }

    public Long getSeriesId() {
        return seriesId;
    }

    public List<ReservationResponse> getCreated() {
        return created;
    }

    public List<FailedSlot> getFailed() {
        return failed;
    }

    public static class FailedSlot {
        private OffsetDateTime startTime;
        private OffsetDateTime endTime;
        private String reason;

        public FailedSlot(OffsetDateTime startTime, OffsetDateTime endTime, String reason) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.reason = reason;
        }

        public OffsetDateTime getStartTime() {
            return startTime;
        }

        public OffsetDateTime getEndTime() {
            return endTime;
        }

        public String getReason() {
            return reason;
        }
    }
}
