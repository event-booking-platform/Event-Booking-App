package com.eventbooking.repository;

import com.eventbooking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserIdOrderByBookingDateDesc(Long userId);

    List<Booking> findByEventId(Long eventId);

    @Query("SELECT b FROM Booking b WHERE b.event.id = :eventId AND b.isReserved = true AND b.status = 'PENDING' AND b.reservationExpiry > :now")
    List<Booking> findActiveReservationsByEventId(@Param("eventId") Long eventId, @Param("now") Instant now);

    @Query("SELECT b FROM Booking b WHERE b.isReserved = true AND b.status = 'PENDING' AND b.reservationExpiry <= :now")
    List<Booking> findExpiredReservations(@Param("now") Instant now);

    @Query("SELECT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.event WHERE b.id = :id")
    Optional<Booking> findByIdWithUserAndEvent(@Param("id") Long id);

    @Query("SELECT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.event WHERE b.id = :id AND b.isReserved = true")
    Optional<Booking> findReservationById(@Param("id") Long id);

    @Query("SELECT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.event WHERE b.id = :id AND b.user.id = :userId")
    Optional<Booking> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}