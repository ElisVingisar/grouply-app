package ee.grouply.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Invitation to an event. Can point to a registered user (invitee) or an email for
 * not-yet-registered users. Token is used for invite links.
 */
@Entity
@Table(name = "event_invitation", indexes = {
    @Index(name = "idx_invitation_token", columnList = "token"),
    @Index(name = "idx_invitation_email", columnList = "email")
})
public class EventInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitee_id")
    private User invitee;

    @Column(length = 320)
    private String email;

    @Column(length = 1000)
    private String message;

    @Column(unique = true, nullable = false, length = 64)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus status = InvitationStatus.PENDING;

    private LocalDateTime sentAt;

    private LocalDateTime respondedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_id")
    private User invitedBy;

    public EventInvitation() {
        this.token = UUID.randomUUID().toString();
        this.sentAt = LocalDateTime.now();
        this.status = InvitationStatus.PENDING;
    }

    public EventInvitation(Event event, String email, User invitedBy, String message) {
        this();
        this.event = event;
        this.email = email;
        this.invitedBy = invitedBy;
        this.message = message;
    }

    // getters / setters
    public Long getId() { return id; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public User getInvitee() { return invitee; }
    public void setInvitee(User invitee) { this.invitee = invitee; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public InvitationStatus getStatus() { return status; }
    public void setStatus(InvitationStatus status) { this.status = status; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public LocalDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }

    public User getInvitedBy() { return invitedBy; }
    public void setInvitedBy(User invitedBy) { this.invitedBy = invitedBy; }

    // helpers
    public void markAccepted() {
        this.status = InvitationStatus.ACCEPTED;
        this.respondedAt = LocalDateTime.now();
    }

    public void markDeclined() {
        this.status = InvitationStatus.DECLINED;
        this.respondedAt = LocalDateTime.now();
    }

    public enum InvitationStatus {
        PENDING,
        ACCEPTED,
        DECLINED
    }
}