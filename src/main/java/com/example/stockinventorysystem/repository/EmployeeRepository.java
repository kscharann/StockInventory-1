package com.example.stockinventorysystem.repository;

import com.example.stockinventorysystem.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByEmpId(String empId);
    Optional<Employee> findByEmpId(String empId);
}
