package com.eventbooking.service;

import com.eventbooking.dto.EventDTO;
import com.eventbooking.entity.Event;
import org.springframework.stereotype.Service;

@Service
public class EventMapper {

    public EventDTO toEventDTO(Event event) {
        if (event == null) {
            return null;
        }

        EventDTO dto = new EventDTO();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setEventDate(event.getEventDate());
        dto.setEventTime(event.getEventTime());
        dto.setVenue(event.getVenue());
        dto.setTicketPrice(event.getTicketPrice());
        dto.setAvailableTickets(event.getAvailableTickets());
        dto.setCategory(event.getCategory());

        return dto;
}
}
