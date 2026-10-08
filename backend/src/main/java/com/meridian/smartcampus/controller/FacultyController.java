package com.meridian.smartcampus.controller;

import com.meridian.smartcampus.model.Faculty;
import com.meridian.smartcampus.repository.FacultyRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faculty")
public class FacultyController {

    private final FacultyRepository facultyRepository;

    public FacultyController(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    @GetMapping
    public ResponseEntity<List<Faculty>> listFaculty(@RequestParam(value = "search", required = false) String search) {
        if (search != null && !search.trim().isEmpty()) {
            return ResponseEntity.ok(facultyRepository.findByNameContainingIgnoreCase(search.trim()));
        }
        return ResponseEntity.ok(facultyRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Faculty> createFaculty(@RequestBody Faculty faculty) {
        if (faculty.getId() == null) {
            long maxId = facultyRepository.findAll().stream()
                    .mapToLong(f -> f.getId() != null ? f.getId() : 0)
                    .max().orElse(0);
            faculty.setId(maxId + 1);
        }
        if (faculty.getFacultyId() == null || faculty.getFacultyId().isEmpty()) {
            faculty.setFacultyId("F" + (1000 + faculty.getId()));
        }
        return ResponseEntity.ok(facultyRepository.save(faculty));
    }
}
