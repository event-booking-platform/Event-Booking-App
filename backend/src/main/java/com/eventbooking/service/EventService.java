package com.eventbooking.service;

import com.eventbooking.dto.UpdateEventDTO;
import com.eventbooking.entity.Event;
import com.eventbooking.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    public List<Event> getAllEvents() {
        return eventRepository.findAvailableEvents();
    }

    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }

    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

    public List<Event> getEventsByCategory(String category) {
        return eventRepository.findByCategoryOrderByEventDate(category);
    }

    public Event updateEventTickets(Long eventId, Integer ticketsSold) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (event.getAvailableTickets() < ticketsSold) {
            throw new RuntimeException("Not enough tickets available");
        }

        event.setAvailableTickets(event.getAvailableTickets() - ticketsSold);
        return eventRepository.save(event);
    }

    public Event updateEvent(Long eventId, UpdateEventDTO updateEventDTO) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        if (updateEventDTO.getTitle() != null) {
            event.setTitle(updateEventDTO.getTitle());
        }
        if (updateEventDTO.getDescription() != null) {
            event.setDescription(updateEventDTO.getDescription());
        }
        if (updateEventDTO.getEventDate() != null) {
            event.setEventDate(updateEventDTO.getEventDate());
        }
        if (updateEventDTO.getEventTime() != null) {
            event.setEventTime(updateEventDTO.getEventTime());
        }
        if (updateEventDTO.getVenue() != null) {
            event.setVenue(updateEventDTO.getVenue());
        }
        if (updateEventDTO.getTicketPrice() != null) {
            event.setTicketPrice(updateEventDTO.getTicketPrice());
        }
        if (updateEventDTO.getAvailableTickets() != null) {
            event.setAvailableTickets(updateEventDTO.getAvailableTickets());
        }
        if (updateEventDTO.getCategory() != null) {
            event.setCategory(updateEventDTO.getCategory());
        }

        return eventRepository.save(event);
    }

    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        eventRepository.delete(event);
    }
}
