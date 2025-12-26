package com.example.labmanagementsystembackend.dto.response;

import java.time.LocalTime;

public class LabResponse {
    private Long id;
    private String name;
    private String location;
    private Integer capacity;
    private LocalTime openTimeStart;
    private LocalTime openTimeEnd;
    private String status;

    public LabResponse(Long id, String name, String location, Integer capacity, LocalTime openTimeStart, LocalTime openTimeEnd, String status) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.capacity = capacity;
        this.openTimeStart = openTimeStart;
        this.openTimeEnd = openTimeEnd;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public LocalTime getOpenTimeStart() {
        return openTimeStart;
    }

    public LocalTime getOpenTimeEnd() {
        return openTimeEnd;
    }

    public String getStatus() {
        return status;
    }
}
