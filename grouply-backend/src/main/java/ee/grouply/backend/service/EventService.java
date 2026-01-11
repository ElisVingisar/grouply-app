package ee.grouply.backend.service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ee.grouply.backend.entity.Event;
import ee.grouply.backend.dto.EventCreateDTO;
import ee.grouply.backend.dto.EventDTO;
import ee.grouply.backend.repo.EventRepository;
import ee.grouply.backend.repo.UserRepository;
import ee.grouply.backend.entity.User;
import ee.grouply.backend.error.NotFoundException;
import org.springframework.transaction.annotation.Transactional;
import ee.grouply.backend.dto.ParticipantDTO;
import java.util.List;

@Service
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventService(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public EventDTO createEvent(EventCreateDTO dto, String email) {
        log.debug("Creating event'{}' for user {}", dto.getTitle(), email);

        User user = findUserByEmail(email);

        Event event = new Event();
        event.setCapacity(dto.getCapacity());
        event.setCreator(user);
        event.setDateTime(dto.getDateTime());
        event.setDescription(dto.getDescription());
        event.setImageUrl(dto.getImageUrl());
        event.setLocation(dto.getLocation());
        event.setTitle(dto.getTitle());
        event.addParticipant(user); // The creator is automatically participant

        Event saved = eventRepository.save(event);

        log.info("Created event id={} title='{}'' by user={}", saved.getId(), saved.getTitle(), email);

        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<EventDTO> getEventsForUser(String email) {
        log.debug("Fetching events for user {}", email);

        User user = findUserByEmail(email);

        List<Event> events = eventRepository.findAll().stream()
                .filter(e -> isUserRelatedToEvent(user, e))
                .toList();

        log.debug("Found {} events for user {}", events.size(), email);
        return events.stream().map(this::toDto).toList();
    }

    // TODO: Add auth - can the user see this?
    @Transactional(readOnly = true)
    public EventDTO getEventById(Long id) {
        log.debug("Fetching event id={}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event not found with id: " + id));

        return toDto(event);
    }

    // TODO: Add auth - only creator can update
    @Transactional
    public EventDTO updateEvent(Long id, EventCreateDTO dto, String email) {
        log.debug("Updating event id={} by user {}", id, email);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event not found with id: " + id));

        // TODO: Check if user is creator
        // User user = findUserByEmail(email);
        // if (!event.getCreator().getId().equals(user.getId())) {
        //     throw new ForbiddenException("Only creator can update event");
        // }

        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setDateTime(dto.getDateTime());
        event.setLocation(dto.getLocation());
        event.setCapacity(dto.getCapacity());
        event.setImageUrl(dto.getImageUrl());

        Event saved = eventRepository.save(event);
        log.info("Updated event id={}", id);

        return toDto(saved);
    }

    // TODO: Add auth - only creator can delete
    @Transactional
    public void deleteEvent(Long id, String email) {
        log.debug("Deleting event id={} by user {}", id, email);

        if (!eventRepository.existsById(id)) {
            throw new NotFoundException("Event not found with id: " + id);
        }

        // TODO: Check if user is creator

        eventRepository.deleteById(id);
        log.info("Deleted event id={}", id);
    }

    @Transactional(readOnly = true)
    public List<ParticipantDTO> getParticipants(Long eventId) {
        log.debug("Fetching participants for event id={}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));

        return event.getParticipants().stream()
                .map(u -> new ParticipantDTO(u.getId(), u.getName(), u.getEmail()))
                .toList();
    }


    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));
    }

    private boolean isUserRelatedToEvent(User user, Event event) {
        boolean isCreator = event.getCreator() != null
                && event.getCreator().getId().equals(user.getId());
        boolean isParticipant = event.getParticipants().contains(user);
        return isCreator || isParticipant;
    }

    private EventDTO toDto(Event event) {
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
}
