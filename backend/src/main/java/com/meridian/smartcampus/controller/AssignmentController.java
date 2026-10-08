package com.meridian.smartcampus.controller;

import com.meridian.smartcampus.model.Assignment;
import com.meridian.smartcampus.model.AssignmentSubmission;
import com.meridian.smartcampus.model.Subject;
import com.meridian.smartcampus.repository.AssignmentRepository;
import com.meridian.smartcampus.repository.AssignmentSubmissionRepository;
import com.meridian.smartcampus.repository.SubjectRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final SubjectRepository subjectRepository;
    private final com.meridian.smartcampus.repository.StudentRepository studentRepository;
    private final com.meridian.smartcampus.repository.NotificationRepository notificationRepository;

    public AssignmentController(AssignmentRepository assignmentRepository,
                                AssignmentSubmissionRepository submissionRepository,
                                SubjectRepository subjectRepository,
                                com.meridian.smartcampus.repository.StudentRepository studentRepository,
                                com.meridian.smartcampus.repository.NotificationRepository notificationRepository) {
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.subjectRepository = subjectRepository;
        this.studentRepository = studentRepository;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    public ResponseEntity<List<Assignment>> listAssignments(
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestHeader(value = "X-Username", required = false) String username) {
        List<Assignment> assignments = assignmentRepository.findAll();
        boolean isFaculty = "faculty".equalsIgnoreCase(userRole);

        // Check if student has submitted each assignment
        for (Assignment a : assignments) {
            List<AssignmentSubmission> subs = submissionRepository.findByAssignmentId(a.getId());
            a.setSubmissions(subs.size());

            if (username != null) {
                Optional<AssignmentSubmission> mySubOpt = subs.stream().filter(s -> 
                        username.equalsIgnoreCase(s.getStudentUsername()) || 
                        (s.getStudentName() != null && s.getStudentName().equalsIgnoreCase(username))
                ).findFirst();

                if (mySubOpt.isPresent()) {
                    if (!isFaculty) {
                        a.setSubmitted(true);
                        a.setStatus("submitted");
                    }
                    a.setMySubmission(mySubOpt.get());
                } else {
                    if (!isFaculty) {
                        a.setSubmitted(false);
                    }
                }
            }

            List<String> completedNames = subs.stream().map(AssignmentSubmission::getStudentName).toList();
            List<String> pendingNames = studentRepository.findAll().stream()
                    .map(com.meridian.smartcampus.model.Student::getName)
                    .filter(name -> !completedNames.contains(name))
                    .limit(5)
                    .toList();

            Map<String, List<String>> status = new HashMap<>();
            status.put("completed", completedNames);
            status.put("incomplete", pendingNames);
            a.setCompletionStatus(status);
        }
        return ResponseEntity.ok(assignments);
    }

    @PostMapping
    public ResponseEntity<Assignment> createAssignment(@RequestBody Assignment assignment) {
        if (assignment.getId() == null) {
            long maxId = assignmentRepository.findAll().stream()
                    .mapToLong(a -> a.getId() != null ? a.getId() : 0)
                    .max().orElse(0);
            assignment.setId(maxId + 1);
        }
        if (assignment.getSubject() == null && assignment.getSubjectId() != null) {
            subjectRepository.findById(assignment.getSubjectId())
                    .ifPresent(s -> assignment.setSubject(s.getName() + " (" + s.getCode() + ")"));
        }
        if (assignment.getStatus() == null) {
            assignment.setStatus("due");
        }
        Assignment saved = assignmentRepository.save(assignment);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<Map<String, Object>> submitAssignment(
            @PathVariable("id") Long id,
            @RequestBody(required = false) Map<String, Object> body,
            @RequestHeader(value = "X-Username", required = false) String headerUsername) {
        
        Optional<Assignment> assignmentOpt = assignmentRepository.findById(id);
        if (assignmentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Assignment a = assignmentOpt.get();
        a.setSubmitted(true);
        a.setStatus("submitted");

        String username = body != null && body.get("studentUsername") != null 
                ? (String) body.get("studentUsername") 
                : (headerUsername != null ? headerUsername : "student1cse");
        
        Optional<com.meridian.smartcampus.model.Student> studentOpt = studentRepository.findAll().stream()
                .filter(st -> (st.getEmail() != null && st.getEmail().toLowerCase().startsWith(username.toLowerCase() + "@")) ||
                              (st.getName() != null && st.getName().equalsIgnoreCase(username)))
                .findFirst();

        Long studentId = studentOpt.map(com.meridian.smartcampus.model.Student::getId).orElse(1L);
        String studentName = studentOpt.map(com.meridian.smartcampus.model.Student::getName).orElse(username.toUpperCase() + " Student");
        String department = studentOpt.map(com.meridian.smartcampus.model.Student::getDepartment).orElse("CSE");

        if (body != null && body.get("studentName") != null) {
            studentName = (String) body.get("studentName");
        }
        if (body != null && body.get("studentId") != null) {
            studentId = ((Number) body.get("studentId")).longValue();
        }
        if (body != null && body.get("department") != null) {
            department = (String) body.get("department");
        }

        // Extract file upload fields
        String fileName = body != null ? (String) body.get("fileName") : null;
        String fileType = body != null ? (String) body.get("fileType") : null;
        String fileSize = body != null ? (String) body.get("fileSize") : null;
        String fileData = body != null ? (String) body.get("fileData") : null;
        String note = body != null && body.containsKey("note") ? (String) body.get("note") : "Uploaded assignment submission";

        // Check if existing submission to update or create new
        Optional<AssignmentSubmission> existingSub = submissionRepository.findByAssignmentIdAndStudentId(id, studentId);
        AssignmentSubmission sub;
        if (existingSub.isPresent()) {
            sub = existingSub.get();
        } else {
            sub = new AssignmentSubmission();
            long maxSubId = submissionRepository.findAll().stream().mapToLong(s -> s.getId() != null ? s.getId() : 0).max().orElse(0);
            sub.setId(maxSubId + 1);
        }

        sub.setAssignmentId(id);
        sub.setStudentId(studentId);
        sub.setStudentName(studentName);
        sub.setStudentUsername(username);
        sub.setDepartment(department);
        sub.setNote(note);
        sub.setStatus("submitted");
        sub.setSubmittedAt(LocalDateTime.now().toString().substring(0, 16).replace("T", " "));
        sub.setFileName(fileName);
        sub.setFileType(fileType);
        sub.setFileSize(fileSize);
        sub.setFileData(fileData);

        submissionRepository.save(sub);

        // Update count
        List<AssignmentSubmission> subs = submissionRepository.findByAssignmentId(id);
        a.setSubmissions(subs.size());
        assignmentRepository.save(a);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Assignment work with file submitted successfully",
                "submissionId", sub.getId()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Assignment> getAssignment(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestHeader(value = "X-Username", required = false) String username) {
        Optional<Assignment> aOpt = assignmentRepository.findById(id);
        if (aOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Assignment a = aOpt.get();
        List<AssignmentSubmission> subs = submissionRepository.findByAssignmentId(a.getId());
        a.setSubmissions(subs.size());
        if (username != null) {
            Optional<AssignmentSubmission> mySubOpt = subs.stream().filter(s ->
                    username.equalsIgnoreCase(s.getStudentUsername()) ||
                    (s.getStudentName() != null && s.getStudentName().equalsIgnoreCase(username))
            ).findFirst();
            if (mySubOpt.isPresent()) {
                if (!"faculty".equalsIgnoreCase(userRole)) {
                    a.setSubmitted(true);
                    a.setStatus("submitted");
                }
                a.setMySubmission(mySubOpt.get());
            }
        }
        return ResponseEntity.ok(a);
    }

    @GetMapping("/{id}/my-submission")
    public ResponseEntity<AssignmentSubmission> getMySubmission(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-Username", required = false) String username) {
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.ok(null);
        }
        List<AssignmentSubmission> subs = submissionRepository.findByAssignmentId(id);
        Optional<AssignmentSubmission> mySubOpt = subs.stream().filter(s ->
                username.equalsIgnoreCase(s.getStudentUsername()) ||
                (s.getStudentName() != null && s.getStudentName().equalsIgnoreCase(username))
        ).findFirst();
        return ResponseEntity.ok(mySubOpt.orElse(null));
    }

    @GetMapping("/{id}/submissions")
    public ResponseEntity<List<AssignmentSubmission>> getAssignmentSubmissions(@PathVariable("id") Long id) {
        List<AssignmentSubmission> subs = submissionRepository.findByAssignmentId(id);
        return ResponseEntity.ok(subs);
    }

    @GetMapping("/{id}/students-status")
    public ResponseEntity<Map<String, Object>> getStudentsCompletionStatus(@PathVariable("id") Long id) {
        Optional<Assignment> aOpt = assignmentRepository.findById(id);
        if (aOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Assignment assignment = aOpt.get();

        List<com.meridian.smartcampus.model.Student> allStudents = studentRepository.findAll();
        List<AssignmentSubmission> submissions = submissionRepository.findByAssignmentId(id);

        List<Map<String, Object>> completedList = new ArrayList<>();
        List<Map<String, Object>> pendingList = new ArrayList<>();

        for (com.meridian.smartcampus.model.Student s : allStudents) {
            String derivedUsername = s.getEmail() != null && s.getEmail().contains("@")
                    ? s.getEmail().substring(0, s.getEmail().indexOf("@"))
                    : "student" + s.getId();

            Optional<AssignmentSubmission> subOpt = submissions.stream()
                    .filter(sub -> (sub.getStudentId() != null && sub.getStudentId().equals(s.getId())) ||
                            (sub.getStudentUsername() != null && (sub.getStudentUsername().equalsIgnoreCase(derivedUsername) || sub.getStudentUsername().equalsIgnoreCase(s.getStudentId()))) ||
                            (sub.getStudentName() != null && sub.getStudentName().equalsIgnoreCase(s.getName())))
                    .findFirst();

            Map<String, Object> sMap = new HashMap<>();
            sMap.put("id", s.getId());
            sMap.put("studentId", s.getStudentId());
            sMap.put("name", s.getName());
            sMap.put("username", derivedUsername);
            sMap.put("email", s.getEmail());
            sMap.put("department", s.getDepartment());
            sMap.put("year", s.getYear());
            sMap.put("section", s.getSection());

            if (subOpt.isPresent()) {
                AssignmentSubmission sub = subOpt.get();
                sMap.put("submittedAt", sub.getSubmittedAt());
                sMap.put("note", sub.getNote());
                sMap.put("fileName", sub.getFileName());
                sMap.put("fileType", sub.getFileType());
                sMap.put("fileSize", sub.getFileSize());
                sMap.put("fileData", sub.getFileData());
                sMap.put("submissionId", sub.getId());
                if (sub.getStudentUsername() != null) {
                    sMap.put("username", sub.getStudentUsername());
                }
                completedList.add(sMap);
            } else {
                pendingList.add(sMap);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("assignment", assignment);
        result.put("completed", completedList);
        result.put("incomplete", pendingList);
        result.put("completedCount", completedList.size());
        result.put("incompleteCount", pendingList.size());
        result.put("totalStudents", allStudents.size());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/alert")
    public ResponseEntity<Map<String, Object>> alertStudent(
            @PathVariable("id") Long id,
            @RequestBody Map<String, Object> body) {
        
        Optional<Assignment> aOpt = assignmentRepository.findById(id);
        if (aOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Assignment assignment = aOpt.get();

        String studentName = (String) body.get("studentName");
        String studentUsername = body.get("studentUsername") != null 
                ? (String) body.get("studentUsername") 
                : (String) body.get("username");
        Long studentId = body.get("studentId") != null ? ((Number) body.get("studentId")).longValue() : null;

        // Create a reminder notification
        long maxNotifId = notificationRepository.findAll().stream()
                .mapToLong(n -> n.getId() != null ? n.getId() : 0)
                .max().orElse(0);

        com.meridian.smartcampus.model.Notification notif = new com.meridian.smartcampus.model.Notification();
        notif.setId(maxNotifId + 1);
        notif.setTitle("Assignment Deadline Alert: " + assignment.getTitle());
        notif.setMessage("Professor alert: You have not submitted the assignment '" + assignment.getTitle() + "' (" + assignment.getSubject() + "). Due date: " + assignment.getDueDate() + ". Please upload your solution.");
        notif.setRead(false);
        notif.setCreatedAt("Just now");
        notif.setTargetRole("student");
        notif.setTargetUsername(studentUsername);
        notif.setStudentId(studentId);

        notificationRepository.save(notif);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Alert sent to " + (studentName != null ? studentName : "student") + "! Notification created."
        ));
    }
}
