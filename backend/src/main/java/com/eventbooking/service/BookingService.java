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
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
@EnableAsync
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventMapper eventMapper;

    private final ConcurrentHashMap<Long, Lock> eventLocks = new ConcurrentHashMap<>();

    @Async
    @Transactional
    public CompletableFuture<ReservationDTO> reserveTickets(Long userId, Long eventId, Integer ticketCount) {
        return CompletableFuture.supplyAsync(() -> {
            Lock eventLock = eventLocks.computeIfAbsent(eventId, k -> new ReentrantLock());

            try {
                if (eventLock.tryLock(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    try {
                        return performReservation(userId, eventId, ticketCount);
                    } finally {
                        eventLock.unlock();
                    }
                } else {
                    throw new RuntimeException("System busy. Please try again in a moment.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Reservation interrupted. Please try again.");
            }
        });
    }

    private ReservationDTO performReservation(Long userId, Long eventId, Integer ticketCount) {
        System.out.println("=== RESERVATION DEBUG START ===");
        System.out.println("User: " + userId + ", Event: " + eventId + ", Tickets: " + ticketCount);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        System.out.println("User and Event loaded successfully");

        int reservedTickets = getReservedTicketsCount(eventId);
        int actuallyAvailable = event.getAvailableTickets() - reservedTickets;
        System.out.println("Available tickets: " + actuallyAvailable);

        if (actuallyAvailable < ticketCount) {
            System.out.println("NOT ENOUGH TICKETS - Available: " + actuallyAvailable + ", Requested: " + ticketCount);
            throw new RuntimeException(
                    "Not enough tickets available. Available: " + actuallyAvailable + ", Requested: " + ticketCount);
        }

        Instant expiryTime = Instant.now().plus(5, ChronoUnit.MINUTES);
        System.out.println("Setting expiry time to (UTC): " + expiryTime);

        Booking reservation = new Booking(user, event, ticketCount);
        reservation.setStatus(BookingStatus.PENDING);
        reservation.setIsReserved(true);
        reservation.setReservationExpiry(expiryTime);

        System.out.println("Before save - Expiry: " + reservation.getReservationExpiry());

        Booking savedReservation = bookingRepository.save(reservation);

        System.out.println("After save - ID: " + savedReservation.getId() + ", Expiry: "
                + savedReservation.getReservationExpiry());

        ReservationDTO dto = convertToReservationDTO(savedReservation);
        System.out.println(
                "Final DTO - Expiry: " + dto.getReservationExpiry() + ", Seconds Left: " + dto.getSecondsRemaining());
        System.out.println("=== RESERVATION DEBUG END ===");

        return dto;
    }

    @Async
    @Transactional
    public CompletableFuture<List<ReservationDTO>> bulkReserveTickets(List<ReservationRequest> requests) {
        return CompletableFuture.supplyAsync(() -> {
            return requests.parallelStream()
                    .map(request -> {
                        try {
                            return reserveTickets(request.getUserId(), request.getEventId(), request.getTicketCount())
                                    .get(10, java.util.concurrent.TimeUnit.SECONDS);
                        } catch (Exception e) {
                            System.err.println("Failed to process reservation for user " + request.getUserId() +
                                    ", event " + request.getEventId() + ": " + e.getMessage());
                            return null;
                        }
                    })
                    .filter(result -> result != null)
                    .collect(Collectors.toList());
        });
    }

    @Async
    @Transactional
    public CompletableFuture<BookingDTO> confirmReservation(Long reservationId) {
        return CompletableFuture.supplyAsync(() -> {
            Booking reservation = bookingRepository.findByIdWithUserAndEvent(reservationId)
                    .orElseThrow(() -> new RuntimeException("Reservation not found"));

            if (reservation.getReservationExpiry() != null
                    && Instant.now().isAfter(reservation.getReservationExpiry())) {
                throw new RuntimeException("Reservation has expired");
            }

            if (!reservation.getIsReserved() || reservation.getStatus() != BookingStatus.PENDING) {
                throw new RuntimeException("Invalid reservation");
            }

            Lock eventLock = eventLocks.computeIfAbsent(reservation.getEvent().getId(), k -> new ReentrantLock());

            try {
                if (eventLock.tryLock(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    try {
                        return performConfirmation(reservation);
                    } finally {
                        eventLock.unlock();
                    }
                } else {
                    throw new RuntimeException("Unable to confirm reservation. Please try again.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Confirmation interrupted. Please try again.");
            }
        });
    }

    private BookingDTO performConfirmation(Booking reservation) {
        Event event = reservation.getEvent();
        User user = reservation.getUser();

        int reservedTickets = getReservedTicketsCount(event.getId());
        int actuallyAvailable = event.getAvailableTickets() - reservedTickets;

        if (actuallyAvailable < reservation.getTicketCount()) {
            throw new RuntimeException("Not enough tickets available");
        }

        event.setAvailableTickets(event.getAvailableTickets() - reservation.getTicketCount());
        eventRepository.save(event);

        reservation.setStatus(BookingStatus.CONFIRMED);
        reservation.setIsReserved(false);
        reservation.setReservationExpiry(null);

        Booking confirmedBooking = bookingRepository.save(reservation);

        return convertBookingToDTO(confirmedBooking, event, user);
    }

    private BookingDTO convertBookingToDTO(Booking booking, Event event, User user) {
        UserDTO userDTO = convertUserToDTO(user);
        EventDTO eventDTO = eventMapper.toEventDTO(event);

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

    @Async
    @Transactional
    public CompletableFuture<Void> cancelReservation(Long reservationId) {
        return CompletableFuture.runAsync(() -> {
            Booking reservation = bookingRepository.findByIdWithUserAndEvent(reservationId)
                    .orElseThrow(() -> new RuntimeException("Reservation not found"));

            if (reservation.getIsReserved() && reservation.getStatus() == BookingStatus.PENDING) {
                Lock eventLock = eventLocks.computeIfAbsent(reservation.getEvent().getId(), k -> new ReentrantLock());

                try {
                    if (eventLock.tryLock(5, java.util.concurrent.TimeUnit.SECONDS)) {
                        try {
                            bookingRepository.delete(reservation);
                        } finally {
                            eventLock.unlock();
                        }
                    } else {
                        throw new RuntimeException("Unable to cancel reservation. Please try again.");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Cancellation interrupted. Please try again.");
                }
            } else {
                throw new RuntimeException("Cannot cancel non-pending reservation");
            }
        });
    }

    private synchronized int getReservedTicketsCount(Long eventId) {
        Instant now = Instant.now();
        List<Booking> activeReservations = bookingRepository.findActiveReservationsByEventId(eventId, now);
        return activeReservations.stream()
                .mapToInt(Booking::getTicketCount)
                .sum();
    }

    @Scheduled(fixedRate = 60000)
    @Async
    @Transactional
    public CompletableFuture<Void> cleanupExpiredReservations() {
        return CompletableFuture.runAsync(() -> {
            Instant now = Instant.now();
            List<Booking> expiredReservations = bookingRepository.findExpiredReservations(now);

            expiredReservations.parallelStream().forEach(reservation -> {
                try {
                    System.out.println("Cleaning up expired reservation: " + reservation.getId());
                    bookingRepository.delete(reservation);
                } catch (Exception e) {
                    System.err.println("Failed to cleanup reservation " + reservation.getId() + ": " + e.getMessage());
                }
            });

            if (!expiredReservations.isEmpty()) {
                System.out.println("Cleaned up " + expiredReservations.size() + " expired reservations");
            }
        });
    }

    public ReservationDTO convertToReservationDTO(Booking reservation) {
        System.out.println("Converting reservation ID: " + reservation.getId());

        Event event = reservation.getEvent();
        if (event == null) {
            System.out.println("WARNING: Event is null in reservation");
            event = eventRepository.findById(reservation.getEvent().getId())
                    .orElseThrow(() -> new RuntimeException("Event not found"));
        }

        EventDTO eventDTO = eventMapper.toEventDTO(event);

        System.out.println("Reservation expiry in entity: " + reservation.getReservationExpiry());

        ReservationDTO dto = new ReservationDTO(
                reservation.getId(),
                reservation.getBookingReference(),
                reservation.getTicketCount(),
                reservation.getTotalAmount(),
                reservation.getReservationExpiry(),
                eventDTO);

        if (reservation.getReservationExpiry() != null) {
            Instant now = Instant.now();
            Instant expiry = reservation.getReservationExpiry();

            long secondsRemaining = Duration.between(now, expiry).getSeconds();
            dto.setSecondsRemaining(Math.max(0, (int) secondsRemaining));

            System.out.println("Seconds remaining: " + secondsRemaining);
            System.out.println("Current time (UTC): " + now);
            System.out.println("Expiry time (UTC): " + expiry);
        } else {
            System.out.println("ERROR: Reservation expiry is NULL");
            dto.setSecondsRemaining(0);
        }

        return dto;
    }

    public Booking createReservation(Long userId, Long eventId, Integer ticketCount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        int reservedTickets = getReservedTicketsCount(eventId);
        int actuallyAvailable = event.getAvailableTickets() - reservedTickets;

        if (actuallyAvailable < ticketCount) {
            throw new RuntimeException(
                    "Not enough tickets available. Available: " + actuallyAvailable + ", Requested: " + ticketCount);
        }

        if (ticketCount <= 0) {
            throw new RuntimeException("Ticket count must be positive");
        }

        Booking reservation = new Booking(user, event, ticketCount);
        reservation.setStatus(BookingStatus.PENDING);
        reservation.setIsReserved(true);
        reservation.setReservationExpiry(Instant.now().plusSeconds(300));

        return bookingRepository.save(reservation);
    }

    @Transactional
    public Booking createBooking(Long userId, Long eventId, Integer ticketCount) {
        Lock eventLock = eventLocks.computeIfAbsent(eventId, k -> new ReentrantLock());

        try {
            if (eventLock.tryLock(5, java.util.concurrent.TimeUnit.SECONDS)) {
                try {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found"));

                    Event event = eventRepository.findById(eventId)
                            .orElseThrow(() -> new RuntimeException("Event not found"));

                    if (event.getAvailableTickets() < ticketCount) {
                        throw new RuntimeException("Not enough tickets available. Available: "
                                + event.getAvailableTickets() + ", Requested: " + ticketCount);
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
                } finally {
                    eventLock.unlock();
                }
            } else {
                throw new RuntimeException("System busy. Please try again.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Booking interrupted. Please try again.");
        }
    }

    @Transactional(readOnly = true)
    public List<Booking> getUserBookings(Long userId) {
        return bookingRepository.findByUserIdOrderByBookingDateDesc(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public BookingDTO convertToDTO(Booking booking) {
        Event event = booking.getEvent();
        User user = booking.getUser();

        if (event == null || user == null) {
            Booking freshBooking = bookingRepository.findByIdWithUserAndEvent(booking.getId())
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            event = freshBooking.getEvent();
            user = freshBooking.getUser();
        }

        UserDTO userDTO = convertUserToDTO(user);
        EventDTO eventDTO = eventMapper.toEventDTO(event);

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

    public static class ReservationRequest {
        private Long userId;
        private Long eventId;
        private Integer ticketCount;

        public ReservationRequest(Long userId, Long eventId, Integer ticketCount) {
            this.userId = userId;
            this.eventId = eventId;
            this.ticketCount = ticketCount;
        }

        public Long getUserId() {
            return userId;
        }

        public Long getEventId() {
            return eventId;
        }

        public Integer getTicketCount() {
            return ticketCount;
        }
    }

    @Scheduled(fixedRate = 300000)
    public void cleanupEventLocks() {
        eventLocks.entrySet().removeIf(entry -> {
            return !isEventActive(entry.getKey());
        });
    }

    private boolean isEventActive(Long eventId) {
        Optional<Event> event = eventRepository.findById(eventId);
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        return event.isPresent() && event.get().getEventDate().isAfter(nowUtc.toLocalDate());
    }

    @Async
    @Transactional
    public CompletableFuture<Void> cancelBooking(Long bookingId) {
        return CompletableFuture.runAsync(() -> {
            Booking booking = bookingRepository.findByIdWithUserAndEvent(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            if (booking.getStatus() != BookingStatus.CONFIRMED) {
                throw new RuntimeException("Only confirmed bookings can be cancelled");
            }

            if (booking.getEvent().getEventDate().isBefore(LocalDate.now())) {
                throw new RuntimeException("Cannot cancel past events");
            }

            Event event = booking.getEvent();
            event.setAvailableTickets(event.getAvailableTickets() + booking.getTicketCount());
            eventRepository.save(event);

            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);

            System.out.println(" Booking cancelled: " + bookingId + ", tickets returned: " + booking.getTicketCount());
        });
    }

    public boolean canCancelBooking(Booking booking) {
        return booking.getStatus() == BookingStatus.CONFIRMED &&
                booking.getEvent().getEventDate().isAfter(LocalDate.now());
    }
}