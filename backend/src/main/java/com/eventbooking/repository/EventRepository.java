package com.eventbooking.repository;

import com.eventbooking.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e ORDER BY e.eventDate, e.eventTime")
    List<Event> findAvailableEvents();

    List<Event> findByCategoryOrderByEventDate(String category);
}