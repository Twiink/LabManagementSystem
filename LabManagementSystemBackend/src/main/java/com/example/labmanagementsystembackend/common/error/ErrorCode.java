package com.example.labmanagementsystembackend.common.error;

public enum ErrorCode {
    RESERVATION_WINDOW_INVALID(4001, "Reservation time window invalid"),
    RESOURCE_CONFLICT(4002, "Resource conflict"),
    RESOURCE_UNAVAILABLE(4003, "Resource unavailable"),
    QUOTA_EXCEEDED(4004, "Reservation quota exceeded"),
    PERMISSION_DENIED(4005, "Permission denied"),
    VALIDATION_FAILED(4006, "Validation failed"),
    INVALID_STATE(4007, "Invalid reservation state"),
    APPROVAL_REQUIRED(4008, "Approval requirements not met"),
    NOT_FOUND(4009, "Resource not found"),
    UNAUTHORIZED(4010, "Unauthorized"),
    INTERNAL_ERROR(5000, "Internal server error");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
