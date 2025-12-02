package com.eventbooking.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class ReservationDTO {
    private Long reservationId;
    private String bookingReference;
    private Integer ticketCount;
    private BigDecimal totalAmount;
    private Instant reservationExpiry;
    private EventDTO event;
    private Integer secondsRemaining;

    // Constructors
    public ReservationDTO() {}

    public ReservationDTO(Long reservationId, String bookingReference, Integer ticketCount, 
                         BigDecimal totalAmount, Instant reservationExpiry, EventDTO event) {
        this.reservationId = reservationId;
        this.bookingReference = bookingReference;
        this.ticketCount = ticketCount;
        this.totalAmount = totalAmount;
        this.reservationExpiry = reservationExpiry;
        this.event = event;
    }

    // Getters and Setters
    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }

    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }

    public Integer getTicketCount() { return ticketCount; }
    public void setTicketCount(Integer ticketCount) { this.ticketCount = ticketCount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public Instant getReservationExpiry() { return reservationExpiry; }
    public void setReservationExpiry(Instant reservationExpiry) { this.reservationExpiry = reservationExpiry; }

    public EventDTO getEvent() { return event; }
    public void setEvent(EventDTO event) { this.event = event; }

    public Integer getSecondsRemaining() { return secondsRemaining; }
    public void setSecondsRemaining(Integer secondsRemaining) { this.secondsRemaining = secondsRemaining; }
}