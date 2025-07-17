package com.example.stockinventorysystem.controller;

import com.example.stockinventorysystem.dto.EmployeeRequest;
import com.example.stockinventorysystem.dto.EmployeeResponse;
import com.example.stockinventorysystem.dto.MessageResponse;
import com.example.stockinventorysystem.model.Employee;
import com.example.stockinventorysystem.repository.EmployeeRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    @Autowired
    EmployeeRepository employeeRepository;

    @GetMapping
    // @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        List<EmployeeResponse> employeeResponses = employees.stream()
                .map(this::convertToEmployeeResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(employeeResponses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getEmployeeById(@PathVariable Long id) {
        return employeeRepository.findById(id)
                .map(employee -> ResponseEntity.ok(convertToEmployeeResponse(employee)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/emp/{empId}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getEmployeeByEmpId(@PathVariable String empId) {
        return employeeRepository.findByEmpId(empId)
                .map(employee -> ResponseEntity.ok(convertToEmployeeResponse(employee)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> createEmployee(@Valid @RequestBody EmployeeRequest employeeRequest) {
        if (employeeRepository.existsByEmpId(employeeRequest.getEmpId())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Employee ID already exists!"));
        }

        Employee employee = new Employee(
                employeeRequest.getEmpId(),
                employeeRequest.getFirstName(),
                employeeRequest.getLastName(),
                employeeRequest.getRole());

        employeeRepository.save(employee);
        return ResponseEntity.ok(convertToEmployeeResponse(employee));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeRequest employeeRequest) {
        return employeeRepository.findById(id)
                .map(employee -> {
                    employee.setFirstName(employeeRequest.getFirstName());
                    employee.setLastName(employeeRequest.getLastName());
                    employee.setRole(employeeRequest.getRole());
                    employeeRepository.save(employee);
                    return ResponseEntity.ok(convertToEmployeeResponse(employee));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        return employeeRepository.findById(id)
                .map(employee -> {
                    employeeRepository.delete(employee);
                    return ResponseEntity.ok(new MessageResponse("Employee deleted successfully!"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private EmployeeResponse convertToEmployeeResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getEmpId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getRole());
    }
}