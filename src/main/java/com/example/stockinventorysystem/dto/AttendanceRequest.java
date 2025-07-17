package com.example.stockinventorysystem.dto;

import org.springframework.web.multipart.MultipartFile;

public class AttendanceRequest {
    private String empId;
    private String attendanceDate; // e.g., "2025-07-17"
    private String timeIn;         // e.g., "08:45:00"
    private MultipartFile image;

    // Getters and Setters
    public String getEmpId() { return empId; }
    public void setEmpId(String empId) { this.empId = empId; }

    public String getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(String attendanceDate) { this.attendanceDate = attendanceDate; }

    public String getTimeIn() { return timeIn; }
    public void setTimeIn(String timeIn) { this.timeIn = timeIn; }

    public MultipartFile getImage() { return image; }
    public void setImage(MultipartFile image) { this.image = image; }
}
