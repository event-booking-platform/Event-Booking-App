package com.eventbooking.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @NotNull
    private Integer ticketCount;

    @NotNull
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Column(name = "booking_date")
    private LocalDateTime bookingDate;

    private String bookingReference;

    @Column(name = "reservation_expiry")
    private LocalDateTime reservationExpiry;

    @Column(name = "is_reserved")
    private Boolean isReserved = false;

    @PrePersist
    protected void onCreate() {
        bookingDate = LocalDateTime.now();
        if (status == null) {
            status = BookingStatus.CONFIRMED;
        }
        if (bookingReference == null) {
            bookingReference = "BK" + System.currentTimeMillis();
        }

        // Calculate total amount
        if (event != null && ticketCount != null) {
            this.totalAmount = event.getTicketPrice().multiply(BigDecimal.valueOf(ticketCount));
        }
    }

    // Constructors
    public Booking() {
    }

    public Booking(User user, Event event, Integer ticketCount) {
        this.user = user;
        this.event = event;
        this.ticketCount = ticketCount;
        this.totalAmount = event.getTicketPrice().multiply(BigDecimal.valueOf(ticketCount));
        this.status = BookingStatus.PENDING;
        this.isReserved = true;
        this.reservationExpiry = LocalDateTime.now().plusMinutes(5); // 5-minute reservation
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Integer getTicketCount() {
        return ticketCount;
    }

    public void setTicketCount(Integer ticketCount) {
        this.ticketCount = ticketCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public LocalDateTime getReservationExpiry() {
        return reservationExpiry;
    }

    public void setReservationExpiry(LocalDateTime reservationExpiry) {
        this.reservationExpiry = reservationExpiry;
    }

    public Boolean getIsReserved() {
        return isReserved;
    }

    public void setIsReserved(Boolean isReserved) {
        this.isReserved = isReserved;
    }

    public boolean isReservationExpired() {
        return isReserved && reservationExpiry != null && LocalDateTime.now().isAfter(reservationExpiry);
    }
}