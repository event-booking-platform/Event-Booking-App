package com.eventbooking.controller;

import com.eventbooking.dto.EventDTO;
import com.eventbooking.dto.UpdateEventDTO;
import com.eventbooking.entity.User;
import com.eventbooking.service.EventMapper;
import com.eventbooking.service.EventService;
import com.eventbooking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private EventService eventService;

    @Autowired
    private EventMapper eventMapper;

    @PostMapping("/users/{userId}/promote")
    public ResponseEntity<?> promoteToAdmin(@PathVariable Long userId) {
        try {
            User promotedUser = userService.promoteToAdmin(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User promoted to admin successfully");
            response.put("user", promotedUser);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> adminDashboard() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Welcome to Admin Dashboard");
        response.put("access", "You have administrative privileges");

        return ResponseEntity.ok(response);
    }

    @PutMapping("/events/{eventId}")
    public ResponseEntity<?> updateEvent(@PathVariable Long eventId, @RequestBody UpdateEventDTO updateEventDTO) {
        try {
            var updatedEvent = eventService.updateEvent(eventId, updateEventDTO);
            EventDTO eventDTO = eventMapper.toEventDTO(updatedEvent);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Event updated successfully");
            response.put("event", eventDTO);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long eventId) {
        try {
            eventService.deleteEvent(eventId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Event deleted successfully");
            response.put("eventId", eventId);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}