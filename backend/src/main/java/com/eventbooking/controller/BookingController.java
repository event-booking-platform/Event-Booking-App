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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
@Transactional
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

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

            ReservationDTO reservation = bookingService.reserveTickets(user.getId(), eventId, ticketCount)
                    .get(30, TimeUnit.SECONDS);

            Map<String, Object> response = new HashMap<>();
            response.put("reservationId", reservation.getReservationId());
            response.put("message", "Tickets reserved successfully");
            response.put("bookingReference", reservation.getBookingReference());
            response.put("expiresInSeconds", reservation.getSecondsRemaining());
            response.put("reservationExpiry", reservation.getReservationExpiry());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = createErrorResponse(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/reservations/{reservationId}/confirm")
    public ResponseEntity<?> confirmReservation(@PathVariable Long reservationId) {
        try {

            BookingDTO booking = bookingService.confirmReservation(reservationId)
                    .get(30, TimeUnit.SECONDS);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Booking confirmed successfully");
            response.put("booking", booking);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = createErrorResponse(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @DeleteMapping("/reservations/{reservationId}")
    public ResponseEntity<?> cancelReservation(@PathVariable Long reservationId) {
        try {
            bookingService.cancelReservation(reservationId)
                    .get(30, TimeUnit.SECONDS);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Reservation cancelled successfully");
            response.put("reservationId", reservationId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = createErrorResponse(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
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
            Map<String, Object> errorResponse = createErrorResponse(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<?> cancelBooking(@PathVariable Long bookingId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Booking booking = bookingService.getBookingById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            if (!booking.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("You can only cancel your own bookings");
            }

            bookingService.cancelBooking(bookingId)
                    .get(30, TimeUnit.SECONDS);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Booking cancelled successfully");
            response.put("bookingId", bookingId);
            response.put("ticketsReturned", booking.getTicketCount());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = createErrorResponse(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/reservations/active")
    public ResponseEntity<?> getActiveReservations() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Booking> userBookings = bookingService.getUserBookings(user.getId());

            List<ReservationDTO> activeReservations = userBookings.stream()
                    .filter(booking -> Boolean.TRUE.equals(booking.getIsReserved()) &&
                            booking.getStatus() == BookingStatus.PENDING &&
                            booking.getReservationExpiry() != null &&
                            Instant.now().isBefore(booking.getReservationExpiry()))
                    .map(bookingService::convertToReservationDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(activeReservations);

        } catch (Exception e) {
            Map<String, Object> errorResponse = createErrorResponse(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", true);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", Instant.now().toString());
        return errorResponse;
    }
}