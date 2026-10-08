package com.meridian.smartcampus.controller;

import com.meridian.smartcampus.model.Student;
import com.meridian.smartcampus.model.Subject;
import com.meridian.smartcampus.model.User;
import com.meridian.smartcampus.repository.StudentRepository;
import com.meridian.smartcampus.repository.SubjectRepository;
import com.meridian.smartcampus.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    public SubjectController(SubjectRepository subjectRepository,
                             StudentRepository studentRepository,
                             UserRepository userRepository) {
        this.subjectRepository = subjectRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<Subject>> listSubjects(
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "semester", required = false) Integer semester,
            @RequestParam(value = "year", required = false) Integer year) {
        
        List<Subject> all = subjectRepository.findAll();
        List<Subject> filtered = all.stream()
                .filter(s -> department == null || department.trim().isEmpty() || (s.getDepartment() != null && s.getDepartment().equalsIgnoreCase(department.trim())))
                .filter(s -> semester == null || (s.getSemester() != null && s.getSemester().equals(semester)))
                .filter(s -> year == null || (s.getYear() != null && s.getYear().equals(year)))
                .toList();

        return ResponseEntity.ok(filtered);
    }

    @GetMapping("/my-subjects")
    public ResponseEntity<List<Subject>> getMySubjects(
            @RequestHeader(value = "X-Username", required = false) String username,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestParam(value = "semester", required = false) Integer semesterParam) {

        String cleanUsername = username != null ? username.trim().toLowerCase() : "";
        String cleanRole = userRole != null ? userRole.trim().toLowerCase() : "student";

        if ("faculty".equalsIgnoreCase(cleanRole)) {
            List<Subject> facultySubjects = subjectRepository.findAll().stream()
                    .filter(s -> (s.getFacultyUsername() != null && s.getFacultyUsername().equalsIgnoreCase(cleanUsername))
                            || (cleanUsername.startsWith("teacher") && s.getFacultyUsername() != null && s.getFacultyUsername().contains(cleanUsername)))
                    .toList();
            if (!facultySubjects.isEmpty()) {
                return ResponseEntity.ok(facultySubjects);
            }
        }

        // Try to match student
        Optional<Student> studentOpt = Optional.empty();
        if (!cleanUsername.isEmpty()) {
            studentOpt = studentRepository.findByEmail(cleanUsername + "@meridian.edu");
            if (studentOpt.isEmpty()) {
                studentOpt = studentRepository.findAll().stream()
                        .filter(s -> s.getEmail() != null && (s.getEmail().toLowerCase().startsWith(cleanUsername + "@") || s.getEmail().toLowerCase().contains(cleanUsername)))
                        .findFirst();
            }
        }

        if (studentOpt.isEmpty()) {
            studentOpt = studentRepository.findById(1L);
        }

        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            String dept = student.getDepartment();
            Integer targetSemester = semesterParam != null ? semesterParam : student.getSemester();
            if (targetSemester == null) targetSemester = 1;

            final int finalSemester = targetSemester;
            List<Subject> subjects = subjectRepository.findAll().stream()
                    .filter(s -> dept == null || s.getDepartment() == null || s.getDepartment().equalsIgnoreCase(dept))
                    .filter(s -> s.getSemester() != null && s.getSemester().equals(finalSemester))
                    .toList();

            if (!subjects.isEmpty()) {
                return ResponseEntity.ok(subjects);
            }

            // If none found for that specific semester, return all subjects in student's department
            List<Subject> deptSubjects = subjectRepository.findAll().stream()
                    .filter(s -> dept == null || s.getDepartment() == null || s.getDepartment().equalsIgnoreCase(dept))
                    .toList();
            if (!deptSubjects.isEmpty()) {
                return ResponseEntity.ok(deptSubjects);
            }
        }

        return ResponseEntity.ok(subjectRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Subject> createSubject(@RequestBody Subject subject) {
        if (subject.getId() == null) {
            long maxId = subjectRepository.findAll().stream()
                    .mapToLong(s -> s.getId() != null ? s.getId() : 0)
                    .max().orElse(0);
            subject.setId(maxId + 1);
        }
        return ResponseEntity.ok(subjectRepository.save(subject));
    }
}
