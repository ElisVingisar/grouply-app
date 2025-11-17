package ee.grouply.backend.api;

import ee.grouply.backend.domain.Event;
import ee.grouply.backend.domain.User;
import ee.grouply.backend.dto.EventCreateDTO;
import ee.grouply.backend.dto.EventDTO;
import ee.grouply.backend.repo.EventRepository;
import ee.grouply.backend.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventRepository eventRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    public EventController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    // Adding a new event
    @PostMapping
    public EventDTO createEvent(@RequestBody EventCreateDTO dto, @AuthenticationPrincipal UserDetails currentUser) {
        User user = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setDateTime(dto.getDateTime());
        event.setLocation(dto.getLocation());
        event.setCapacity(dto.getCapacity());
        event.setImageUrl(dto.getImageUrl());
        event.setCreator(user); // Set creator
        event.addParticipant(user); // Creator is automatically a participant
        
        Event saved = eventRepository.save(event);
        return toDTO(saved);
    }

    // All events
    @GetMapping
    public List<EventDTO> getAllEvents(@AuthenticationPrincipal UserDetails currentUser) {
        User user = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Find events where user is creator or participant
        List<Event> events = eventRepository.findAll().stream()
                .filter(e -> 
                    (e.getCreator() != null && e.getCreator().getId().equals(user.getId())) ||
                    e.getParticipants().contains(user)
                )
                .collect(Collectors.toList());
        
        return events.stream().map(this::toDTO).collect(Collectors.toList());
    }

    // One event → return DTO (not entity)
    @GetMapping("/{id}")
    public EventDTO one(@PathVariable Long id) {
        Event e = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event with id " + id + " not found"));
        return toDTO(e);
    }

    // Update event → accept DTO, return DTO (not entity)
    @PutMapping("/{id}")
    public EventDTO update(@PathVariable Long id, @RequestBody EventCreateDTO updated) {
        Event e = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event with id " + id + " not found"));

        e.setTitle(updated.getTitle());
        e.setDescription(updated.getDescription());
        e.setDateTime(updated.getDateTime());
        e.setLocation(updated.getLocation());
        e.setCapacity(updated.getCapacity());
        e.setImageUrl(updated.getImageUrl());

        Event saved = eventRepository.save(e);
        return toDTO(saved);
    }

    // Delete an event
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        if (!eventRepository.existsById(id)) {
            throw new RuntimeException("Event with id " + id + " not found");
        }
        eventRepository.deleteById(id);
    }

    // Helper method to convert Event to EventDTO
    private EventDTO toDTO(Event event) {
        EventDTO dto = new EventDTO();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setDateTime(event.getDateTime());
        dto.setLocation(event.getLocation());
        dto.setCapacity(event.getCapacity());
        dto.setImageUrl(event.getImageUrl());
        return dto;
    }

    /**
     * Get all participants of an event (creator + accepted invitations)
     * GET /api/events/{eventId}/participants
     */
    @GetMapping("/{eventId}/participants")
    public List<UserDTO> getParticipants(@PathVariable Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        
        return event.getParticipants().stream()
                .map(u -> {
                    UserDTO dto = new UserDTO();
                    dto.id = u.getId();
                    dto.name = u.getName();
                    dto.email = u.getEmail();
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public static class UserDTO {
        public Long id;
        public String name;
        public String email;
    }
}
