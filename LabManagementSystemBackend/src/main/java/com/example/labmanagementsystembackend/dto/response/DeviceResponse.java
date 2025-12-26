package com.example.labmanagementsystembackend.dto.response;

public class DeviceResponse {
    private Long id;
    private Long labId;
    private String name;
    private String model;
    private String status;

    public DeviceResponse(Long id, Long labId, String name, String model, String status) {
        this.id = id;
        this.labId = labId;
        this.name = name;
        this.model = model;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Long getLabId() {
        return labId;
    }

    public String getName() {
        return name;
    }

    public String getModel() {
        return model;
    }

    public String getStatus() {
        return status;
    }
}
