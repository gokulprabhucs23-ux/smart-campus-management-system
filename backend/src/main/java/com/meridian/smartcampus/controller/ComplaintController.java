package com.meridian.smartcampus.controller;

import com.meridian.smartcampus.model.Complaint;
import com.meridian.smartcampus.repository.ComplaintRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintRepository complaintRepository;
    private final com.meridian.smartcampus.repository.NotificationRepository notificationRepository;

    public ComplaintController(ComplaintRepository complaintRepository,
                               com.meridian.smartcampus.repository.NotificationRepository notificationRepository) {
        this.complaintRepository = complaintRepository;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    public ResponseEntity<List<Complaint>> listComplaints(
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestHeader(value = "X-Username", required = false) String username) {
        return ResponseEntity.ok(complaintRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Complaint> createComplaint(
            @RequestBody Complaint complaint,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "student") String userRole,
            @RequestHeader(value = "X-Username", required = false) String username) {
        if (complaint.getId() == null) {
            long maxId = complaintRepository.findAll().stream()
                    .mapToLong(c -> c.getId() != null ? c.getId() : 0)
                    .max().orElse(0);
            complaint.setId(maxId + 1);
        }
        if (complaint.getCreatedAt() == null) {
            complaint.setCreatedAt("Just now");
        }
        if (complaint.getStatus() == null) {
            complaint.setStatus("submitted");
        }
        if (complaint.getSenderRole() == null) {
            complaint.setSenderRole("faculty".equalsIgnoreCase(userRole) ? "faculty" : "student");
        }
        if (complaint.getSenderUsername() == null && username != null) {
            complaint.setSenderUsername(username);
        }
        if (complaint.getStudent() == null) {
            complaint.setStudent(username != null ? username : ("faculty".equalsIgnoreCase(userRole) ? "Teacher" : "Student"));
        }

        Complaint saved = complaintRepository.save(complaint);

        // Notify Admin of new complaint
        long maxNotifId = notificationRepository.findAll().stream().mapToLong(n -> n.getId() != null ? n.getId() : 0).max().orElse(0);
        com.meridian.smartcampus.model.Notification adminNotif = new com.meridian.smartcampus.model.Notification();
        adminNotif.setId(maxNotifId + 1);
        adminNotif.setTitle("New Grievance Submitted to Administrator");
        adminNotif.setMessage("[" + saved.getCategory() + "] " + saved.getTitle() + " - from " + saved.getStudent() + " (" + saved.getSenderRole() + ")");
        adminNotif.setTargetRole("admin");
        adminNotif.setCreatedAt("Just now");
        adminNotif.setRead(false);
        notificationRepository.save(adminNotif);

        return ResponseEntity.ok(saved);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Complaint> updateComplaintStatus(
            @PathVariable("id") Long id,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        Optional<Complaint> complaintOpt = complaintRepository.findById(id);
        if (complaintOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Complaint complaint = complaintOpt.get();
        if (body.containsKey("status")) {
            String newStatus = (String) body.get("status");
            complaint.setStatus(newStatus);

            // Notify the student / teacher of the status update
            long maxNotifId = notificationRepository.findAll().stream().mapToLong(n -> n.getId() != null ? n.getId() : 0).max().orElse(0);
            com.meridian.smartcampus.model.Notification userNotif = new com.meridian.smartcampus.model.Notification();
            userNotif.setId(maxNotifId + 1);
            userNotif.setTitle("Complaint Status Updated: " + newStatus.toUpperCase());
            userNotif.setMessage("Administrator updated your grievance '" + complaint.getTitle() + "' status to " + newStatus + ".");
            userNotif.setTargetUsername(complaint.getSenderUsername());
            userNotif.setTargetRole(complaint.getSenderRole());
            userNotif.setCreatedAt("Just now");
            userNotif.setRead(false);
            notificationRepository.save(userNotif);
        }
        return ResponseEntity.ok(complaintRepository.save(complaint));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteComplaint(@PathVariable("id") Long id) {
        complaintRepository.findById(id).ifPresent(complaintRepository::delete);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
