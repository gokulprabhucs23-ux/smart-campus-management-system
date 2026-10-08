package com.meridian.smartcampus.controller;

import com.meridian.smartcampus.model.CampusEvent;
import com.meridian.smartcampus.repository.EventRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventRepository eventRepository;

    public EventController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @GetMapping
    public ResponseEntity<List<CampusEvent>> listEvents() {
        return ResponseEntity.ok(eventRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<CampusEvent> createEvent(@RequestBody CampusEvent event) {
        if (event.getId() == null) {
            long maxId = eventRepository.findAll().stream()
                    .mapToLong(e -> e.getId() != null ? e.getId() : 0)
                    .max().orElse(0);
            event.setId(maxId + 1);
        }
        if (event.getRegistered() == null) {
            event.setRegistered(0);
        }
        if (event.getCapacity() == null) {
            event.setCapacity(500);
        }
        return ResponseEntity.ok(eventRepository.save(event));
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<Map<String, Object>> registerForEvent(@PathVariable("id") Long id) {
        Optional<CampusEvent> eventOpt = eventRepository.findById(id);
        if (eventOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        CampusEvent event = eventOpt.get();
        event.setRegisteredByMe(true);
        event.setRegistered(event.getRegistered() != null ? event.getRegistered() + 1 : 1);
        eventRepository.save(event);

        return ResponseEntity.ok(Map.of("success", true, "message", "Registered for event successfully"));
    }
}
