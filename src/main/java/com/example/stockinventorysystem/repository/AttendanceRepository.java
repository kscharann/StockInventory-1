package com.example.stockinventorysystem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.stockinventorysystem.model.Attendance;
import com.example.stockinventorysystem.model.Employee;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByEmployee(Employee employee);  // changed from empId string
}
