package com.example.stockinventorysystem.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔁 Mapped relationship to Employee (many attendances per employee)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    private LocalDate attendanceDate;
    private LocalDateTime inOutTime;
    private String imagePath;
    private LocalDateTime imageDateTime;

    public Attendance() {}

    // Getters and Setters
    public Long getId() { return id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }

    public LocalDateTime getInOutTime() { return inOutTime; }
    public void setInOutTime(LocalDateTime inOutTime) { this.inOutTime = inOutTime; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public LocalDateTime getImageDateTime() { return imageDateTime; }
    public void setImageDateTime(LocalDateTime imageDateTime) { this.imageDateTime = imageDateTime; }
}
