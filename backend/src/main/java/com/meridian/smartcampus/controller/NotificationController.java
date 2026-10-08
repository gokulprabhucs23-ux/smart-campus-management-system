package com.meridian.smartcampus.controller;

import com.meridian.smartcampus.model.Notification;
import com.meridian.smartcampus.repository.NotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> listNotifications(
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestHeader(value = "X-Username", required = false) String username) {
        List<Notification> all = notificationRepository.findAll();
        if (userRole == null && username == null) {
            return ResponseEntity.ok(all);
        }

        String role = userRole != null ? userRole.toLowerCase().trim() : "student";
        List<Notification> filtered = all.stream().filter(n -> {
            // Check username match
            if (username != null && n.getTargetUsername() != null) {
                return username.equalsIgnoreCase(n.getTargetUsername());
            }
            // If targeted to a specific username and current user is not that username, skip
            if (n.getTargetUsername() != null) {
                return false;
            }
            // Check role match or broadcast
            return n.getTargetRole() == null || role.equalsIgnoreCase(n.getTargetRole());
        }).toList();

        return ResponseEntity.ok(filtered);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable("id") Long id) {
        Optional<Notification> notificationOpt = notificationRepository.findById(id);
        if (notificationOpt.isPresent()) {
            Notification n = notificationOpt.get();
            n.setRead(true);
            notificationRepository.save(n);
        }
        return ResponseEntity.ok(Map.of("success", true));
    }
}
