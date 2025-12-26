package com.example.labmanagementsystembackend.dto.response;

public class CourseResponse {
    private Long id;
    private String name;
    private String className;
    private Integer studentCount;
    private String term;
    private Long createdBy;

    public CourseResponse(Long id, String name, String className, Integer studentCount, String term, Long createdBy) {
        this.id = id;
        this.name = name;
        this.className = className;
        this.studentCount = studentCount;
        this.term = term;
        this.createdBy = createdBy;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getClassName() {
        return className;
    }

    public Integer getStudentCount() {
        return studentCount;
    }

    public String getTerm() {
        return term;
    }

    public Long getCreatedBy() {
        return createdBy;
    }
}
