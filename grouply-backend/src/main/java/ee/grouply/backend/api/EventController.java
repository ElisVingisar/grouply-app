package ee.grouply.backend.api;

import ee.grouply.backend.domain.Event;
import ee.grouply.backend.error.NotFoundException;
import ee.grouply.backend.repo.EventRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventRepository eventRepository;

    public EventController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    // Adding a new event
    @PostMapping
    public Event addEvent(@RequestBody Event event) {
        return eventRepository.save(event);
    }

    // All events
    @GetMapping
    public List<Event> all() {
        return eventRepository.findAll();
    }

    // One event
    @GetMapping("/{id}")
    public Event one(@PathVariable Long id) {
        return  eventRepository.findById(id).orElseThrow(() -> new NotFoundException("Event with id " + id + " not found"));
    }

    // Updating an event
    @PutMapping("/{id}")
    public Event update(@PathVariable Long id, @RequestBody Event updated) {
        return eventRepository.findById(id).map(e -> {
            e.setTitle(updated.getTitle());
            e.setDescription(updated.getDescription());
            e.setDateTime(updated.getDateTime());
            e.setLocation(updated.getLocation());
            e.setCapacity(updated.getCapacity());
            e.setImageUrl(updated.getImageUrl());
            return eventRepository.save(e);
        }).orElseThrow(() -> new NotFoundException("Event with id " + id + " not found"));
    }

    // Delete an event
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        if (!eventRepository.existsById(id)) {
            throw new NotFoundException("Event with id " + id + " not found");
        }
        eventRepository.deleteById(id);
    }
}
