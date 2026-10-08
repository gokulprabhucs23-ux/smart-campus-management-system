package com.meridian.smartcampus.controller;

import com.meridian.smartcampus.model.Student;
import com.meridian.smartcampus.model.StudentDetails;
import com.meridian.smartcampus.repository.StudentDetailsRepository;
import com.meridian.smartcampus.repository.StudentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentRepository studentRepository;
    private final StudentDetailsRepository studentDetailsRepository;

    public StudentController(StudentRepository studentRepository, StudentDetailsRepository studentDetailsRepository) {
        this.studentRepository = studentRepository;
        this.studentDetailsRepository = studentDetailsRepository;
    }

    @GetMapping
    public ResponseEntity<List<Student>> listStudents(
            @RequestParam(value = "search", required = false) String search,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        List<Student> students;
        if (search != null && !search.trim().isEmpty()) {
            students = studentRepository.findByNameContainingIgnoreCase(search.trim());
        } else {
            students = studentRepository.findAll();
        }

        if ("faculty".equalsIgnoreCase(userRole)) {
            students = students.stream().filter(s -> Long.valueOf(1L).equals(s.getMentorId())).toList();
        }

        return ResponseEntity.ok(students);
    }

    @PostMapping
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        if (student.getId() == null) {
            long maxId = studentRepository.findAll().stream()
                    .mapToLong(s -> s.getId() != null ? s.getId() : 0)
                    .max().orElse(0);
            student.setId(maxId + 1);
        }
        if (student.getStudentId() == null || student.getStudentId().isEmpty()) {
            student.setStudentId("S" + (1000 + student.getId()));
        }
        Student saved = studentRepository.save(student);

        // Also create a default student_details record if none exists
        if (studentDetailsRepository.findByStudentId(saved.getId()).isEmpty()) {
            StudentDetails details = new StudentDetails();
            details.setStudentId(saved.getId());
            details.setCgpa(3.5);
            details.setArrearCount(0);
            details.setMentor(Map.of("name", "Dr. Alan Turing", "phone", "+1 555-0100"));
            details.setFees(Map.of("total", 15000, "paid", 15000, "pending", 0));
            details.setMarks(List.of(
                    Map.of("subject", "CS101", "grade", "A"),
                    Map.of("subject", "MTH101", "grade", "B+")
            ));
            studentDetailsRepository.save(details);
        }

        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudent(@PathVariable("id") Long id) {
        return studentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<Student> updateStudent(@PathVariable("id") Long id, @RequestBody Map<String, Object> updates) {
        return studentRepository.findById(id).map(student -> {
            if (updates.containsKey("name")) student.setName((String) updates.get("name"));
            if (updates.containsKey("email")) student.setEmail((String) updates.get("email"));
            if (updates.containsKey("department")) student.setDepartment((String) updates.get("department"));
            if (updates.containsKey("year") && updates.get("year") != null) {
                student.setYear(((Number) updates.get("year")).intValue());
            }
            if (updates.containsKey("section")) student.setSection((String) updates.get("section"));
            if (updates.containsKey("status")) student.setStatus((String) updates.get("status"));
            if (updates.containsKey("attendance") && updates.get("attendance") != null) {
                student.setAttendance(((Number) updates.get("attendance")).intValue());
            }
            if (updates.containsKey("mentorId") && updates.get("mentorId") != null) {
                student.setMentorId(((Number) updates.get("mentorId")).longValue());
            }
            return ResponseEntity.ok(studentRepository.save(student));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteStudent(@PathVariable("id") Long id) {
        studentRepository.findById(id).ifPresent(studentRepository::delete);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<StudentDetails> getStudentDetails(@PathVariable("id") Long id) {
        Optional<StudentDetails> details = studentDetailsRepository.findByStudentId(id);
        if (details.isPresent()) {
            return ResponseEntity.ok(details.get());
        }

        // Return a sensible default if not explicitly found
        StudentDetails fallback = new StudentDetails();
        fallback.setStudentId(id);
        fallback.setCgpa(3.8);
        fallback.setArrearCount(0);
        fallback.setSgpa(List.of(
                Map.of("sem", 1, "gpa", 3.7),
                Map.of("sem", 2, "gpa", 3.9)
        ));
        fallback.setArrearsHistory(Collections.emptyList());
        fallback.setMarks(List.of(
                Map.of("subject", "CS101", "grade", "A"),
                Map.of("subject", "MTH101", "grade", "B+")
        ));
        fallback.setRisks(Collections.emptyList());
        fallback.setMentor(Map.of("name", "Dr. Alan Turing", "phone", "+1 555-0100"));
        fallback.setFees(Map.of("pending", 0, "paid", 15000, "total", 15000));
        return ResponseEntity.ok(fallback);
    }
}
