package com.meridian.smartcampus.controller;

import com.meridian.smartcampus.model.Department;
import com.meridian.smartcampus.repository.DepartmentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    public DepartmentController(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @GetMapping
    public ResponseEntity<List<Department>> listDepartments() {
        return ResponseEntity.ok(departmentRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Department> createDepartment(@RequestBody Department department) {
        if (department.getId() == null) {
            long maxId = departmentRepository.findAll().stream()
                    .mapToLong(d -> d.getId() != null ? d.getId() : 0)
                    .max().orElse(0);
            department.setId(maxId + 1);
        }
        Department saved = departmentRepository.save(department);
        return ResponseEntity.ok(saved);
    }
}
