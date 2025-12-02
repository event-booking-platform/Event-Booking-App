package com.eventbooking.dto;

import com.eventbooking.entity.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingDTO {
    private Long id;
    private Integer ticketCount;
    private BigDecimal totalAmount;
    private BookingStatus status;
    private LocalDateTime bookingDate;
    private String bookingReference;
    private UserDTO user;
    private EventDTO event;

    public BookingDTO() {}

    public BookingDTO(Long id, Integer ticketCount, BigDecimal totalAmount,
                      BookingStatus status, LocalDateTime bookingDate,
                      String bookingReference, UserDTO user, EventDTO event) {
        this.id = id;
        this.ticketCount = ticketCount;
        this.totalAmount = totalAmount;
        this.status = status;
        this.bookingDate = bookingDate;
        this.bookingReference = bookingReference;
        this.user = user;
        this.event = event;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getTicketCount() { return ticketCount; }
    public void setTicketCount(Integer ticketCount) { this.ticketCount = ticketCount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public LocalDateTime getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; }

    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }

    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }

    public EventDTO getEvent() { return event; }
    public void setEvent(EventDTO event) { this.event=event;}
}
