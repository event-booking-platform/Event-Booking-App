package com.eventbooking.controller;

import com.eventbooking.dto.BookingDTO;
import com.eventbooking.dto.ReservationDTO;
import com.eventbooking.entity.Booking;
import com.eventbooking.entity.BookingStatus;
import com.eventbooking.entity.User;
import com.eventbooking.service.BookingService;
import com.eventbooking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody Map<String, Object> bookingRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // Get user from username
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Long eventId = Long.valueOf(bookingRequest.get("eventId").toString());
            Integer ticketCount = Integer.valueOf(bookingRequest.get("ticketCount").toString());

            Booking booking = bookingService.createBooking(user.getId(), eventId, ticketCount);
            BookingDTO bookingDTO = bookingService.convertToDTO(booking);

            return ResponseEntity.ok(bookingDTO);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserBookings() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(bookingService.getUserBookingDTOs(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingById(@PathVariable Long id) {
        return bookingService.getBookingById(id)
                .map(bookingService::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/reserve")
    public ResponseEntity<?> reserveTickets(@RequestBody Map<String, Object> reservationRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Long eventId = Long.valueOf(reservationRequest.get("eventId").toString());
            Integer ticketCount = Integer.valueOf(reservationRequest.get("ticketCount").toString());

            ReservationDTO reservation = bookingService.reserveTickets(user.getId(), eventId, ticketCount);

            return ResponseEntity.ok(reservation);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reservations/{reservationId}/confirm")
    public ResponseEntity<?> confirmReservation(@PathVariable Long reservationId) {
        try {
            BookingDTO booking = bookingService.confirmReservation(reservationId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Booking confirmed successfully");
            response.put("booking", booking);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/reservations/{reservationId}")
    public ResponseEntity<?> cancelReservation(@PathVariable Long reservationId) {
        try {
            bookingService.cancelReservation(reservationId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Reservation cancelled successfully");
            response.put("reservationId", reservationId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/reservations/{reservationId}")
    public ResponseEntity<?> getReservation(@PathVariable Long reservationId) {
        try {
            Booking reservation = bookingService.getBookingById(reservationId)
                    .orElseThrow(() -> new RuntimeException("Reservation not found"));

            if (!reservation.getIsReserved() || reservation.getStatus() != BookingStatus.PENDING) {
                throw new RuntimeException("Invalid reservation");
            }

            ReservationDTO reservationDTO = bookingService.convertToReservationDTO(reservation);
            return ResponseEntity.ok(reservationDTO);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}