package com.example.labmanagementsystembackend.dto.response;

public class ReservationCreateResponse {
    private Long reservationId;
    private String status;

    public ReservationCreateResponse(Long reservationId, String status) {
        this.reservationId = reservationId;
        this.status = status;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public String getStatus() {
        return status;
    }
}
