package com.meridian.smartcampus.controller;

import com.meridian.smartcampus.model.LeaveRequest;
import com.meridian.smartcampus.model.Notification;
import com.meridian.smartcampus.repository.LeaveRequestRepository;
import com.meridian.smartcampus.repository.NotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/leaves")
public class LeaveRequestController {

    private final LeaveRequestRepository leaveRequestRepository;
    private final NotificationRepository notificationRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public LeaveRequestController(LeaveRequestRepository leaveRequestRepository,
                                  NotificationRepository notificationRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    public ResponseEntity<List<LeaveRequest>> getLeaves(
            @RequestParam(value = "applicantType", required = false) String applicantType,
            @RequestParam(value = "applicantUsername", required = false) String applicantUsername,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "approverRole", required = false) String approverRole,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "student") String userRole,
            @RequestHeader(value = "X-Username", required = false) String currentUsername) {

        List<LeaveRequest> list = leaveRequestRepository.findAll();

        // Scope filter
        if ("my".equalsIgnoreCase(scope)) {
            String targetUser = (applicantUsername != null && !applicantUsername.isEmpty())
                    ? applicantUsername : currentUsername;
            if (targetUser != null && !targetUser.isEmpty()) {
                list = list.stream()
                        .filter(l -> targetUser.equalsIgnoreCase(l.getApplicantUsername()))
                        .collect(Collectors.toList());
            } else if ("faculty".equalsIgnoreCase(userRole)) {
                list = list.stream()
                        .filter(l -> "faculty".equalsIgnoreCase(l.getApplicantType()))
                        .collect(Collectors.toList());
            } else {
                list = list.stream()
                        .filter(l -> "student".equalsIgnoreCase(l.getApplicantType()))
                        .collect(Collectors.toList());
            }
        } else if ("approvals".equalsIgnoreCase(scope)) {
            if ("faculty".equalsIgnoreCase(userRole)) {
                // Teacher reviews student leave requests
                list = list.stream()
                        .filter(l -> "student".equalsIgnoreCase(l.getApplicantType()))
                        .collect(Collectors.toList());
            } else if ("admin".equalsIgnoreCase(userRole)) {
                // Admin reviews faculty leave requests
                list = list.stream()
                        .filter(l -> "faculty".equalsIgnoreCase(l.getApplicantType()))
                        .collect(Collectors.toList());
            }
        }

        // Specific applicantType filter
        if (applicantType != null && !applicantType.isEmpty() && !"all".equalsIgnoreCase(applicantType)) {
            list = list.stream()
                    .filter(l -> applicantType.equalsIgnoreCase(l.getApplicantType()))
                    .collect(Collectors.toList());
        }

        // Specific username filter
        if (applicantUsername != null && !applicantUsername.isEmpty() && !"my".equalsIgnoreCase(scope)) {
            list = list.stream()
                    .filter(l -> applicantUsername.equalsIgnoreCase(l.getApplicantUsername()))
                    .collect(Collectors.toList());
        }

        // Approver role filter
        if (approverRole != null && !approverRole.isEmpty()) {
            list = list.stream()
                    .filter(l -> approverRole.equalsIgnoreCase(l.getAssignedApproverRole()))
                    .collect(Collectors.toList());
        }

        // Status filter
        if (status != null && !status.isEmpty() && !"all".equalsIgnoreCase(status)) {
            list = list.stream()
                    .filter(l -> status.equalsIgnoreCase(l.getStatus()))
                    .collect(Collectors.toList());
        }

        // Sort descending by ID (newest first)
        list.sort((a, b) -> Long.compare(b.getId() != null ? b.getId() : 0, a.getId() != null ? a.getId() : 0));

        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveRequest> getLeaveById(@PathVariable("id") Long id) {
        return leaveRequestRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<LeaveRequest> createLeaveRequest(
            @RequestBody LeaveRequest request,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "student") String userRole,
            @RequestHeader(value = "X-Username", required = false) String currentUsername) {

        long maxId = leaveRequestRepository.findAll().stream()
                .mapToLong(l -> l.getId() != null ? l.getId() : 0)
                .max().orElse(0);
        request.setId(maxId + 1);

        String nowFormatted = LocalDateTime.now().format(FORMATTER);
        request.setCreatedAt(nowFormatted);
        request.setStatus("PENDING");

        // Determine applicant identity & role
        if ("faculty".equalsIgnoreCase(userRole)) {
            request.setApplicantType("faculty");
            request.setAssignedApproverRole("admin");
            if (request.getAssignedApproverName() == null || request.getAssignedApproverName().isEmpty()) {
                request.setAssignedApproverName("Campus Administrator");
            }
            if (request.getApplicantUsername() == null || request.getApplicantUsername().isEmpty()) {
                request.setApplicantUsername(currentUsername != null ? currentUsername : "teacher1cse");
            }
            if (request.getApplicantName() == null || request.getApplicantName().isEmpty()) {
                request.setApplicantName("Dr. Alan Turing");
            }
            if (request.getDepartment() == null || request.getDepartment().isEmpty()) {
                request.setDepartment("CSE");
            }

            // Create notification for Admin
            createNotification(
                    "admin",
                    null,
                    "New Faculty Leave Request",
                    request.getApplicantName() + " (" + request.getDepartment() + ") requested " +
                            (request.getTotalDays() != null ? request.getTotalDays() + " days of " : "") +
                            request.getLeaveType() + " from " + request.getStartDate() + " to " + request.getEndDate() + "."
            );
        } else {
            // Student request
            request.setApplicantType("student");
            request.setAssignedApproverRole("faculty");
            if (request.getAssignedApproverName() == null || request.getAssignedApproverName().isEmpty()) {
                request.setAssignedApproverName("Dr. Alan Turing (HOD/Mentor)");
            }
            if (request.getApplicantUsername() == null || request.getApplicantUsername().isEmpty()) {
                request.setApplicantUsername(currentUsername != null ? currentUsername : "student1cse");
            }
            if (request.getApplicantName() == null || request.getApplicantName().isEmpty()) {
                request.setApplicantName("Alex Rivera");
            }
            if (request.getDepartment() == null || request.getDepartment().isEmpty()) {
                request.setDepartment("CSE");
            }

            // Create notification for Faculty
            createNotification(
                    "faculty",
                    request.getAssignedApproverUsername(),
                    "New Student Leave Application",
                    "Student " + request.getApplicantName() + " (" + request.getDepartment() + ") applied for " +
                            (request.getTotalDays() != null ? request.getTotalDays() + " days " : "") +
                            request.getLeaveType() + " from " + request.getStartDate() + " to " + request.getEndDate() + "."
            );
        }

        // Validate days
        if (request.getTotalDays() == null || request.getTotalDays() <= 0) {
            request.setTotalDays(1);
        }

        LeaveRequest saved = leaveRequestRepository.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<?> approveLeave(
            @PathVariable("id") Long id,
            @RequestBody(required = false) Map<String, String> body,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "faculty") String userRole,
            @RequestHeader(value = "X-Username", required = false) String currentUsername) {

        Optional<LeaveRequest> opt = leaveRequestRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        LeaveRequest leave = opt.get();

        // Enforce approval hierarchy:
        // Student leaves -> approved by Teacher (faculty) or Admin
        // Teacher leaves -> approved ONLY by Admin
        if ("faculty".equalsIgnoreCase(leave.getApplicantType())) {
            if (!"admin".equalsIgnoreCase(userRole)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only Campus Administrators can approve faculty leave requests."));
            }
        } else if ("student".equalsIgnoreCase(leave.getApplicantType())) {
            if (!"faculty".equalsIgnoreCase(userRole) && !"admin".equalsIgnoreCase(userRole)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only Faculty members (or administrators) can approve student leave requests."));
            }
        }

        String remarks = body != null && body.containsKey("remarks") ? body.get("remarks") : "Approved without conditions.";
        String reviewerName = "admin".equalsIgnoreCase(userRole)
                ? (currentUsername != null ? currentUsername : "Campus Administrator")
                : (currentUsername != null ? currentUsername : "Faculty Reviewer");

        leave.setStatus("APPROVED");
        leave.setReviewedBy(reviewerName);
        leave.setReviewerRemarks(remarks);
        leave.setReviewedAt(LocalDateTime.now().format(FORMATTER));

        LeaveRequest updated = leaveRequestRepository.save(leave);

        // Notify the applicant
        createNotification(
                leave.getApplicantType(),
                leave.getApplicantUsername(),
                "Leave Approved ✅",
                "Your leave application (" + leave.getLeaveType() + ": " + leave.getStartDate() + " to " +
                        leave.getEndDate() + ") has been APPROVED by " + reviewerName + ". Remarks: " + remarks
        );

        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> rejectLeave(
            @PathVariable("id") Long id,
            @RequestBody(required = false) Map<String, String> body,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "faculty") String userRole,
            @RequestHeader(value = "X-Username", required = false) String currentUsername) {

        Optional<LeaveRequest> opt = leaveRequestRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        LeaveRequest leave = opt.get();

        // Enforce approval hierarchy:
        if ("faculty".equalsIgnoreCase(leave.getApplicantType())) {
            if (!"admin".equalsIgnoreCase(userRole)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only Campus Administrators can reject faculty leave requests."));
            }
        } else if ("student".equalsIgnoreCase(leave.getApplicantType())) {
            if (!"faculty".equalsIgnoreCase(userRole) && !"admin".equalsIgnoreCase(userRole)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only Faculty members can reject student leave requests."));
            }
        }

        String remarks = body != null && body.containsKey("remarks") ? body.get("remarks") : "Declined due to academic scheduling conflicts.";
        String reviewerName = "admin".equalsIgnoreCase(userRole)
                ? (currentUsername != null ? currentUsername : "Campus Administrator")
                : (currentUsername != null ? currentUsername : "Faculty Reviewer");

        leave.setStatus("REJECTED");
        leave.setReviewedBy(reviewerName);
        leave.setReviewerRemarks(remarks);
        leave.setReviewedAt(LocalDateTime.now().format(FORMATTER));

        LeaveRequest updated = leaveRequestRepository.save(leave);

        // Notify the applicant
        createNotification(
                leave.getApplicantType(),
                leave.getApplicantUsername(),
                "Leave Application Declined ❌",
                "Your leave application (" + leave.getLeaveType() + ": " + leave.getStartDate() + " to " +
                        leave.getEndDate() + ") was not approved by " + reviewerName + ". Reason: " + remarks
        );

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLeave(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        Optional<LeaveRequest> opt = leaveRequestRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        leaveRequestRepository.delete(opt.get());
        return ResponseEntity.ok(Map.of("success", true, "message", "Leave request removed."));
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
