package com.example.labmanagementsystembackend.dto.response;

import java.util.List;

public class CourseResponse {
    private Long id;
    private String name;
    private String className;
    private Integer studentCount;
    private String term;
    private Long createdBy;
    private Long labId;
    private String labName;
    private String scheduleTime;
    private String teacherName;
    private List<Long> studentIds;

    public CourseResponse(Long id, String name, String className, Integer studentCount, String term, Long createdBy) {
        this.id = id;
        this.name = name;
        this.className = className;
        this.studentCount = studentCount;
        this.term = term;
        this.createdBy = createdBy;
    }

    public CourseResponse(Long id, String name, String className, Integer studentCount, String term, Long createdBy, Long labId, String labName, String scheduleTime, String teacherName) {
        this.id = id;
        this.name = name;
        this.className = className;
        this.studentCount = studentCount;
        this.term = term;
        this.createdBy = createdBy;
        this.labId = labId;
        this.labName = labName;
        this.scheduleTime = scheduleTime;
        this.teacherName = teacherName;
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

    public Long getLabId() {
        return labId;
    }

    public void setLabId(Long labId) {
        this.labId = labId;
    }

    public String getLabName() {
        return labName;
    }

    public void setLabName(String labName) {
        this.labName = labName;
    }

    public String getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(String scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public List<Long> getStudentIds() {
        return studentIds;
    }

    public void setStudentIds(List<Long> studentIds) {
        this.studentIds = studentIds;
    }
}
