package com.example.stockinventorysystem.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AttendanceResponse {
    private Long id;
    private String empId;
    private LocalDateTime inOutTime;
    private String imagePath;
    private LocalDateTime imageDateTime;

    public AttendanceResponse(Long id, String empId, LocalDateTime inOutTime, String imagePath, LocalDateTime imageDateTime, LocalDate attendanceDate) {
        this.id = id;
        this.empId = empId;
        this.inOutTime = inOutTime;
        this.imagePath = imagePath;
        this.imageDateTime = imageDateTime;
        this.attendanceDate = attendanceDate;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmpId() { return empId; }
    public void setEmpId(String empId) { this.empId = empId; }

    public LocalDateTime getInOutTime() { return inOutTime; }
    public void setInOutTime(LocalDateTime inOutTime) { this.inOutTime = inOutTime; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public LocalDateTime getImageDateTime() { return imageDateTime; }
    public void setImageDateTime(LocalDateTime imageDateTime) { this.imageDateTime = imageDateTime; }
}
