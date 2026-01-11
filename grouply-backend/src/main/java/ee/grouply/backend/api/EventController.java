package ee.grouply.backend.api;

import ee.grouply.backend.dto.EventCreateDTO;
import ee.grouply.backend.dto.EventDTO;
import ee.grouply.backend.dto.ParticipantDTO;
import jakarta.validation.Valid;
import ee.grouply.backend.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // Adding a new event
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventDTO createEvent(
            @RequestBody @Valid EventCreateDTO dto, 
            @AuthenticationPrincipal UserDetails currentUser) {
        
        return eventService.createEvent(dto, currentUser.getUsername());
    }

    // All events
    @GetMapping
    public List<EventDTO> getAllEvents(@AuthenticationPrincipal UserDetails currentUser) {
        return eventService.getEventsForUser(currentUser.getUsername());
    }

    // One event
    @GetMapping("/{id}")
    public EventDTO getEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        return eventService.getEventById(id, currentUser.getUsername());
    }

    // Update event
    @PutMapping("/{id}")
    public EventDTO updateEvent(
            @PathVariable Long id, 
            @RequestBody @Valid EventCreateDTO updated,
            @AuthenticationPrincipal UserDetails currentUser) {
        
        return eventService.updateEvent(id, updated, currentUser.getUsername());
    }

    // Delete an event
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        
        eventService.deleteEvent(id, currentUser.getUsername());
    }


    @GetMapping("/{eventId}/participants")
    public List<ParticipantDTO> getParticipants(
            @PathVariable Long eventId,
            @AuthenticationPrincipal UserDetails currentUser) {
        return eventService.getParticipants(eventId, currentUser.getUsername());
    }

}
