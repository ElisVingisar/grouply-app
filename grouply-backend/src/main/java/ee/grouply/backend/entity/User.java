package ee.grouply.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.*;

/**
 * Extended User entity to support authentication and event relations.
 */
@JsonIgnoreProperties({
    "passwordHash",
    "createdEvents",
    "participatingEvents"
})
@Entity
@Table(name = "app_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String passwordHash;

    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
    private List<Event> createdEvents = new ArrayList<>();

    @ManyToMany(mappedBy = "participants", fetch = FetchType.LAZY)
    private Set<Event> participatingEvents = new HashSet<>();

    public User() {}

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // -- getters / setters --

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public List<Event> getCreatedEvents() { return createdEvents; }
    public void setCreatedEvents(List<Event> createdEvents) { this.createdEvents = createdEvents; }

    public Set<Event> getParticipatingEvents() { return participatingEvents; }
    public void setParticipatingEvents(Set<Event> participatingEvents) { this.participatingEvents = participatingEvents; }

    // helper methods
    public void addCreatedEvent(Event e) {
        if (e == null) return;
        createdEvents.add(e);
        e.setCreator(this);
    }

    public void addParticipatingEvent(Event e) {
        if (e == null) return;
        participatingEvents.add(e);
        e.getParticipants().add(this);
    }

    public void removeParticipatingEvent(Event e) {
        if (e == null) return;
        participatingEvents.remove(e);
        e.getParticipants().remove(this);
    }
}