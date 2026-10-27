package com.meridian.smartcampus.controller;

import com.meridian.smartcampus.model.*;
import com.meridian.smartcampus.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentRepository studentRepository;
    private final StudentDetailsRepository studentDetailsRepository;
    private final FacultyRepository facultyRepository;
    private final UserRepository userRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final NotificationRepository notificationRepository;

    public StudentController(StudentRepository studentRepository,
                             StudentDetailsRepository studentDetailsRepository,
                             FacultyRepository facultyRepository,
                             UserRepository userRepository,
                             LeaveRequestRepository leaveRequestRepository,
                             NotificationRepository notificationRepository) {
        this.studentRepository = studentRepository;
        this.studentDetailsRepository = studentDetailsRepository;
        this.facultyRepository = facultyRepository;
        this.userRepository = userRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.notificationRepository = notificationRepository;
    }

    private Long resolveFacultyId(String username) {
        if (username == null || username.trim().isEmpty()) return 1L;
        String clean = username.trim().toLowerCase();
        Optional<Faculty> fOpt = facultyRepository.findByUsername(clean);
        if (fOpt.isPresent()) return fOpt.get().getId();

        fOpt = facultyRepository.findByEmail(clean + "@meridian.edu");
        if (fOpt.isPresent()) return fOpt.get().getId();

        Optional<User> uOpt = userRepository.findByUsername(clean);
        if (uOpt.isPresent() && uOpt.get().getEmail() != null) {
            fOpt = facultyRepository.findByEmail(uOpt.get().getEmail());
            if (fOpt.isPresent()) return fOpt.get().getId();
        }
        return 1L;
    }

    private void enrichStudentWithMentor(Student s) {
        if (s.getMentorId() != null && (s.getMentorName() == null || s.getMentorEmail() == null)) {
            facultyRepository.findById(s.getMentorId()).ifPresent(f -> {
                s.setMentorName(f.getName());
                s.setMentorEmail(f.getEmail());
            });
        }
    }

    @GetMapping
    public ResponseEntity<List<Student>> listStudents(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "mentorId", required = false) Long mentorId,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestHeader(value = "X-Username", required = false) String currentUsername) {

        List<Student> students;
        if (search != null && !search.trim().isEmpty()) {
            students = studentRepository.findByNameContainingIgnoreCase(search.trim());
        } else {
            students = studentRepository.findAll();
        }

        // Filter based on explicit mentorId or role scope
        if (mentorId != null) {
            students = students.stream().filter(s -> mentorId.equals(s.getMentorId())).collect(Collectors.toList());
        } else if ("mentees".equalsIgnoreCase(scope) || ("faculty".equalsIgnoreCase(userRole) && !"all".equalsIgnoreCase(scope))) {
            Long facultyId = resolveFacultyId(currentUsername);
            students = students.stream().filter(s -> facultyId != null && facultyId.equals(s.getMentorId())).collect(Collectors.toList());
        }

        for (Student s : students) {
            enrichStudentWithMentor(s);
        }

        return ResponseEntity.ok(students);
    }

    @GetMapping("/mentees")
    public ResponseEntity<List<Student>> getMyMentees(
            @RequestHeader(value = "X-Username", required = false) String currentUsername) {
        Long facultyId = resolveFacultyId(currentUsername);
        List<Student> mentees = studentRepository.findByMentorId(facultyId);
        for (Student s : mentees) {
            enrichStudentWithMentor(s);
        }
        return ResponseEntity.ok(mentees);
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

        if (student.getMentorId() == null) {
            student.setMentorId(1L);
        }
        enrichStudentWithMentor(student);

        Student saved = studentRepository.save(student);

        // Also create a default student_details record if none exists
        if (studentDetailsRepository.findByStudentId(saved.getId()).isEmpty()) {
            StudentDetails details = new StudentDetails();
            details.setStudentId(saved.getId());
            details.setCgpa(3.5);
            details.setArrearCount(0);

            Faculty f = facultyRepository.findById(saved.getMentorId()).orElse(null);
            if (f != null) {
                Map<String, Object> mentorMap = new LinkedHashMap<>();
                mentorMap.put("id", f.getId());
                mentorMap.put("name", f.getName());
                mentorMap.put("email", f.getEmail());
                mentorMap.put("phone", f.getPhone());
                mentorMap.put("department", f.getDepartment());
                mentorMap.put("designation", f.getDesignation());
                mentorMap.put("username", f.getUsername());
                details.setMentor(mentorMap);
            } else {
                details.setMentor(Map.of("name", "Dr. Alan Turing", "phone", "+1 555-0100"));
            }

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
        return studentRepository.findById(id).map(s -> {
            enrichStudentWithMentor(s);
            return ResponseEntity.ok(s);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/mentor")
    public ResponseEntity<?> assignMentor(@PathVariable("id") Long id, @RequestBody Map<String, Object> body) {
        Optional<Student> studentOpt = studentRepository.findById(id);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Object mentorIdObj = body.get("mentorId");
        if (mentorIdObj == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "mentorId is required"));
        }

        Long newMentorId = ((Number) mentorIdObj).longValue();
        Optional<Faculty> facultyOpt = facultyRepository.findById(newMentorId);
        if (facultyOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Faculty with ID " + newMentorId + " not found"));
        }

        Faculty faculty = facultyOpt.get();
        Student student = studentOpt.get();

        student.setMentorId(newMentorId);
        student.setMentorName(faculty.getName());
        student.setMentorEmail(faculty.getEmail());
        Student updatedStudent = studentRepository.save(student);

        // Synchronize StudentDetails
        Optional<StudentDetails> detailsOpt = studentDetailsRepository.findByStudentId(id);
        StudentDetails details = detailsOpt.orElseGet(() -> {
            StudentDetails d = new StudentDetails();
            d.setStudentId(id);
            return d;
        });

        Map<String, Object> mentorMap = new LinkedHashMap<>();
        mentorMap.put("id", faculty.getId());
        mentorMap.put("name", faculty.getName());
        mentorMap.put("email", faculty.getEmail());
        mentorMap.put("phone", faculty.getPhone());
        mentorMap.put("department", faculty.getDepartment());
        mentorMap.put("designation", faculty.getDesignation());
        mentorMap.put("username", faculty.getUsername());
        details.setMentor(mentorMap);
        studentDetailsRepository.save(details);

        // Update pending leave requests for this student to reflect new assigned mentor
        List<LeaveRequest> pendingLeaves = leaveRequestRepository.findAll().stream()
                .filter(l -> "student".equalsIgnoreCase(l.getApplicantType())
                        && "PENDING".equalsIgnoreCase(l.getStatus())
                        && ((student.getStudentId() != null && student.getStudentId().equalsIgnoreCase(l.getStudentId()))
                            || (student.getEmail() != null && l.getApplicantUsername() != null && student.getEmail().toLowerCase().startsWith(l.getApplicantUsername().toLowerCase()))))
                .toList();

        for (LeaveRequest lr : pendingLeaves) {
            lr.setAssignedApproverUsername(faculty.getUsername());
            lr.setAssignedApproverName(faculty.getName() + " (Mentor)");
            leaveRequestRepository.save(lr);
        }

        // Notify Student
        createNotification("student", student.getEmail() != null ? student.getEmail().split("@")[0] : null,
                "Faculty Mentor Updated",
                faculty.getName() + " (" + faculty.getDepartment() + ") has been assigned as your academic mentor.");

        // Notify Faculty
        createNotification("faculty", faculty.getUsername(),
                "New Mentee Assigned",
                student.getName() + " (" + student.getDepartment() + ", Year " + student.getYear() + ") has been assigned as your mentee.");

        return ResponseEntity.ok(updatedStudent);
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
                Long newMentorId = ((Number) updates.get("mentorId")).longValue();
                student.setMentorId(newMentorId);
                facultyRepository.findById(newMentorId).ifPresent(f -> {
                    student.setMentorName(f.getName());
                    student.setMentorEmail(f.getEmail());

                    // Sync details
                    studentDetailsRepository.findByStudentId(id).ifPresent(d -> {
                        Map<String, Object> mentorMap = new LinkedHashMap<>();
                        mentorMap.put("id", f.getId());
                        mentorMap.put("name", f.getName());
                        mentorMap.put("email", f.getEmail());
                        mentorMap.put("phone", f.getPhone());
                        mentorMap.put("department", f.getDepartment());
                        mentorMap.put("designation", f.getDesignation());
                        mentorMap.put("username", f.getUsername());
                        d.setMentor(mentorMap);
                        studentDetailsRepository.save(d);
                    });
                });
            }
            enrichStudentWithMentor(student);
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
        Optional<Student> studentOpt = studentRepository.findById(id);
        Student student = studentOpt.orElse(null);

        Optional<StudentDetails> detailsOpt = studentDetailsRepository.findByStudentId(id);
        StudentDetails details = detailsOpt.orElseGet(() -> {
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
            fallback.setFees(Map.of("pending", 0, "paid", 15000, "total", 15000));
            return fallback;
        });

        // Ensure mentor details are up-to-date
        if (student != null && student.getMentorId() != null) {
            facultyRepository.findById(student.getMentorId()).ifPresent(f -> {
                Map<String, Object> mentorMap = new LinkedHashMap<>();
                mentorMap.put("id", f.getId());
                mentorMap.put("name", f.getName());
                mentorMap.put("email", f.getEmail());
                mentorMap.put("phone", f.getPhone());
                mentorMap.put("department", f.getDepartment());
                mentorMap.put("designation", f.getDesignation());
                mentorMap.put("username", f.getUsername());
                details.setMentor(mentorMap);
            });
        }

        // Attach all leave requests for this student so the mentor can review complete history
        if (student != null) {
            String studentUsername = student.getEmail() != null ? student.getEmail().split("@")[0] : null;
            String studentIdStr = student.getStudentId();
            List<LeaveRequest> studentLeaves = leaveRequestRepository.findAll().stream()
                    .filter(l -> "student".equalsIgnoreCase(l.getApplicantType())
                            && ((studentIdStr != null && studentIdStr.equalsIgnoreCase(l.getStudentId()))
                                || (studentUsername != null && studentUsername.equalsIgnoreCase(l.getApplicantUsername()))))
                    .sorted((a, b) -> Long.compare(b.getId() != null ? b.getId() : 0, a.getId() != null ? a.getId() : 0))
                    .collect(Collectors.toList());
            details.setLeaves(studentLeaves);
        }

        return ResponseEntity.ok(details);
    }

    private void createNotification(String targetRole, String targetUsername, String title, String message) {
        try {
            long maxNotifId = notificationRepository.findAll().stream()
                    .mapToLong(n -> n.getId() != null ? n.getId() : 0)
                    .max().orElse(0);
            Notification notif = new Notification();
            notif.setId(maxNotifId + 1);
            notif.setTitle(title);
            notif.setMessage(message);
            notif.setTargetRole(targetRole);
            notif.setTargetUsername(targetUsername);
            notif.setCreatedAt("Just now");
            notif.setRead(false);
            notificationRepository.save(notif);
        } catch (Exception ignored) {
        }
    }
}
