package com.example.stockinventorysystem.controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.stockinventorysystem.dto.AttendanceRequest;
import com.example.stockinventorysystem.dto.AttendanceResponse;
import com.example.stockinventorysystem.model.Attendance;
import com.example.stockinventorysystem.repository.AttendanceRepository;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> createAttendance(@ModelAttribute AttendanceRequest attendanceRequest) {
        String imagePath = null;

        // 1. Handle Image Upload
        if (attendanceRequest.getImage() != null && !attendanceRequest.getImage().isEmpty()) {
            try {
                String basePath = "C:/attendance_images/";
                File dir = new File(basePath);
                if (!dir.exists() && !dir.mkdirs()) {
                    return ResponseEntity.badRequest().body("Failed to create upload directory.");
                }

                String fileName = attendanceRequest.getEmpId() + "_" + System.currentTimeMillis() + ".jpg";
                File file = new File(dir, fileName);
                attendanceRequest.getImage().transferTo(file);
                imagePath = basePath + fileName;

                System.out.println("✅ Image uploaded: " + file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.badRequest().body("Image upload failed: " + e.getMessage());
            }
        }

        // 2. Save attendance
        try {
            LocalDateTime inOutTime = LocalDateTime.parse(attendanceRequest.getAttendanceDate() + "T" + attendanceRequest.getTimeIn());
            LocalDateTime imageDateTime = LocalDateTime.now();
            LocalDate attendanceDate = LocalDate.parse(attendanceRequest.getAttendanceDate());

            Attendance attendance = new Attendance();
            attendance.setEmpId(attendanceRequest.getEmpId());
            attendance.setInOutTime(inOutTime);
            attendance.setImagePath(imagePath);
            attendance.setImageDateTime(imageDateTime);
            attendance.setAttendanceDate(attendanceDate);

            attendanceRepository.save(attendance);

            return ResponseEntity.ok(convertToAttendanceResponse(attendance));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to save attendance: " + e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<AttendanceResponse>> getAllAttendance() {
        List<Attendance> attendanceList = attendanceRepository.findAll();
        List<AttendanceResponse> responses = attendanceList.stream()
                .map(this::convertToAttendanceResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/emp/{empId}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceByEmpId(@PathVariable String empId) {
        List<Attendance> attendanceList = attendanceRepository.findByEmpId(empId);
        List<AttendanceResponse> responses = attendanceList.stream()
                .map(this::convertToAttendanceResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // ✅ Updated response mapping including attendanceDate
    private AttendanceResponse convertToAttendanceResponse(Attendance attendance) {
        return new AttendanceResponse(
                attendance.getId(),
                attendance.getEmpId(),
                attendance.getInOutTime(),
                attendance.getImagePath(),
                attendance.getImageDateTime(),
                attendance.getAttendanceDate()
        );
    }
}
