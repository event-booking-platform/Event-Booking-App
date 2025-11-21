package com.eventbooking.service;

import com.eventbooking.dto.BookingDTO;
import com.eventbooking.dto.EventDTO;
import com.eventbooking.dto.ReservationDTO;
import com.eventbooking.dto.UserDTO;
import com.eventbooking.entity.Booking;
import com.eventbooking.entity.BookingStatus;
import com.eventbooking.entity.Event;
import com.eventbooking.entity.User;
import com.eventbooking.repository.BookingRepository;
import com.eventbooking.repository.EventRepository;
import com.eventbooking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventMapper eventMapper;

    // Reserve tickets (temporary hold)
    public ReservationDTO reserveTickets(Long userId, Long eventId, Integer ticketCount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Check available tickets (considering existing reservations)
        int reservedTickets = getReservedTicketsCount(eventId);
        int actuallyAvailable = event.getAvailableTickets() - reservedTickets;

        if (actuallyAvailable < ticketCount) {
            throw new RuntimeException(
                    "Not enough tickets available. Available: " + actuallyAvailable + ", Requested: " + ticketCount);
        }

        if (ticketCount <= 0) {
            throw new RuntimeException("Ticket count must be positive");
        }

        // Create reservation (pending booking)
        Booking reservation = new Booking(user, event, ticketCount);
        reservation.setStatus(BookingStatus.PENDING);
        reservation.setIsReserved(true);
        reservation.setReservationExpiry(LocalDateTime.now().plusMinutes(5));

        Booking savedReservation = bookingRepository.save(reservation);

        // Convert to DTO
        return convertToReservationDTO(savedReservation);
    }

    // Confirm reservation (convert to actual booking)
    public BookingDTO confirmReservation(Long reservationId) {
        Booking reservation = bookingRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        // Check if reservation is still valid
        if (reservation.isReservationExpired()) {
            throw new RuntimeException("Reservation has expired");
        }

        if (!reservation.getIsReserved() || reservation.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Invalid reservation");
        }

        // Update event available tickets
        Event event = reservation.getEvent();
        if (event.getAvailableTickets() < reservation.getTicketCount()) {
            throw new RuntimeException("Not enough tickets available");
        }

        // Convert reservation to confirmed booking
        event.setAvailableTickets(event.getAvailableTickets() - reservation.getTicketCount());
        eventRepository.save(event);

        reservation.setStatus(BookingStatus.CONFIRMED);
        reservation.setIsReserved(false);
        reservation.setReservationExpiry(null);

        Booking confirmedBooking = bookingRepository.save(reservation);

        return convertToDTO(confirmedBooking);
    }

    // Cancel reservation
    public void cancelReservation(Long reservationId) {
        Booking reservation = bookingRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        if (reservation.getIsReserved() && reservation.getStatus() == BookingStatus.PENDING) {
            bookingRepository.delete(reservation);
        } else {
            throw new RuntimeException("Cannot cancel non-pending reservation");
        }
    }

    // Get reserved tickets count for an event
    private int getReservedTicketsCount(Long eventId) {
        List<Booking> activeReservations = bookingRepository.findActiveReservationsByEventId(eventId,
                LocalDateTime.now());
        return activeReservations.stream()
                .mapToInt(Booking::getTicketCount)
                .sum();
    }

    // Scheduled task to clean up expired reservations
    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredReservations() {
        List<Booking> expiredReservations = bookingRepository.findExpiredReservations(LocalDateTime.now());

        for (Booking reservation : expiredReservations) {
            System.out.println("Cleaning up expired reservation: " + reservation.getId());
            bookingRepository.delete(reservation);
        }

        if (!expiredReservations.isEmpty()) {
            System.out.println("Cleaned up " + expiredReservations.size() + " expired reservations");
        }
    }

    // Convert to ReservationDTO
    public ReservationDTO convertToReservationDTO(Booking reservation) {
        EventDTO eventDTO = eventMapper.toEventDTO(reservation.getEvent());

        ReservationDTO dto = new ReservationDTO(
                reservation.getId(),
                reservation.getBookingReference(),
                reservation.getTicketCount(),
                reservation.getTotalAmount(),
                reservation.getReservationExpiry(),
                eventDTO);

        // Calculate seconds remaining
        if (reservation.getReservationExpiry() != null) {
            long secondsRemaining = LocalDateTime.now().until(reservation.getReservationExpiry(), ChronoUnit.SECONDS);
            dto.setSecondsRemaining(Math.max(0, (int) secondsRemaining));
        }

        return dto;
    }

    public Booking createBooking(Long userId, Long eventId, Integer ticketCount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (event.getAvailableTickets() < ticketCount) {
            throw new RuntimeException("Not enough tickets available. Available: " + event.getAvailableTickets()
                    + ", Requested: " + ticketCount);
        }

        if (ticketCount <= 0) {
            throw new RuntimeException("Ticket count must be positive");
        }

        Booking booking = new Booking(user, event, ticketCount);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setIsReserved(false);
        Booking savedBooking = bookingRepository.save(booking);

        event.setAvailableTickets(event.getAvailableTickets() - ticketCount);
        eventRepository.save(event);

        return savedBooking;
    }

    public List<Booking> getUserBookings(Long userId) {
        return bookingRepository.findByUserIdOrderByBookingDateDesc(userId);
    }

    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    public BookingDTO convertToDTO(Booking booking) {
        UserDTO userDTO = convertUserToDTO(booking.getUser());
        EventDTO eventDTO = eventMapper.toEventDTO(booking.getEvent());

        return new BookingDTO(
                booking.getId(),
                booking.getTicketCount(),
                booking.getTotalAmount(),
                booking.getStatus(),
                booking.getBookingDate(),
                booking.getBookingReference(),
                userDTO,
                eventDTO);
    }

    private UserDTO convertUserToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setPhoneNumber(user.getPhoneNumber());
        userDTO.setRole(user.getRole());
        userDTO.setCreatedAt(user.getCreatedAt());
        return userDTO;
    }

    public List<BookingDTO> getUserBookingDTOs(Long userId) {
        List<Booking> bookings = getUserBookings(userId);
        return bookings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}