package com.meridian.smartcampus.controller;

import com.meridian.smartcampus.model.Announcement;
import com.meridian.smartcampus.repository.AnnouncementRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementRepository announcementRepository;

    public AnnouncementController(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    @GetMapping
    public ResponseEntity<List<Announcement>> listAnnouncements() {
        return ResponseEntity.ok(announcementRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Announcement> createAnnouncement(@RequestBody Announcement announcement) {
        if (announcement.getId() == null) {
            long maxId = announcementRepository.findAll().stream()
                    .mapToLong(a -> a.getId() != null ? a.getId() : 0)
                    .max().orElse(0);
            announcement.setId(maxId + 1);
        }
        if (announcement.getPublishedAt() == null) {
            announcement.setPublishedAt("Just now");
        }
        if (announcement.getAuthor() == null) {
            announcement.setAuthor("Facilities / Admin");
        }
        return ResponseEntity.ok(announcementRepository.save(announcement));
    }
}
