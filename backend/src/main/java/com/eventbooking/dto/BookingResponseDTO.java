package com.eventbooking.dto;

import com.eventbooking.entity.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingResponseDTO {
    private Long id;
    private Integer ticketCount;
    private BigDecimal totalAmount;
    private BookingStatus status;
    private LocalDateTime bookingDate;
    private String bookingReference;
    private UserSummaryDTO user;

    // Constructors
    public BookingResponseDTO() {}

    public BookingResponseDTO(Long id, Integer ticketCount, BigDecimal totalAmount,
                              BookingStatus status, LocalDateTime bookingDate,
                              String bookingReference, UserSummaryDTO user) {
        this.id = id;
        this.ticketCount = ticketCount;
        this.totalAmount = totalAmount;
        this.status = status;
        this.bookingDate = bookingDate;
        this.bookingReference = bookingReference;
        this.user = user;
    }

    // Getters and Setters
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

    public UserSummaryDTO getUser() { return user; }
    public void setUser(UserSummaryDTO user) { this.user=user;}
}
