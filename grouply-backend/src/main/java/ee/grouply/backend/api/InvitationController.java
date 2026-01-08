package ee.grouply.backend.api;

import ee.grouply.backend.entity.Event;
import ee.grouply.backend.entity.EventInvitation;
import ee.grouply.backend.entity.User;
import ee.grouply.backend.entity.EventInvitation.InvitationStatus;
import ee.grouply.backend.repo.EventRepository;
import ee.grouply.backend.repo.InvitationRepository;
import ee.grouply.backend.repo.UserRepository;
import ee.grouply.backend.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class InvitationController {

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * Send invitation to a user by email
     * POST /api/events/{eventId}/invitations
     */
    @PostMapping("/events/{eventId}/invitations")
    public ResponseEntity<?> sendInvitation(
            @PathVariable Long eventId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        User inviter = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        // Check if user exists in system
        User invitee = userRepository.findByEmail(email).orElse(null);

        // Check if already invited
        if (invitee != null) {
            boolean alreadyInvited = invitationRepository.existsByEventIdAndInviteeIdAndStatus(
                    eventId, invitee.getId(), InvitationStatus.PENDING
            );
            if (alreadyInvited) {
                return ResponseEntity.badRequest().body(Map.of("error", "User already invited"));
            }
            // Check if already a participant
            if (event.getParticipants().contains(invitee)) {
                return ResponseEntity.badRequest().body(Map.of("error", "User already a participant"));
            }
        } else {
            // Check if email already invited
            boolean alreadyInvited = invitationRepository.existsByEventIdAndEmailAndStatus(
                    eventId, email, InvitationStatus.PENDING
            );
            if (alreadyInvited) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email already invited"));
            }
        }

        EventInvitation invitation = new EventInvitation(event, email, inviter, body.get("message"));
        if (invitee != null) {
            invitation.setInvitee(invitee);
        }
        invitationRepository.save(invitation);

        return ResponseEntity.ok(Map.of("success", true, "invitationId", invitation.getId()));
    }

    /**
     * Get pending invitations for current user
     * GET /api/invitations/pending
     */
    @GetMapping("/invitations/pending")
    public List<Map<String, Object>> getPendingInvitations(@AuthenticationPrincipal UserDetails currentUser) {
        User user = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Direct invitations (already linked)
        List<EventInvitation> invitations = invitationRepository.findByInviteeIdAndStatus(
                user.getId(), InvitationStatus.PENDING
        );

        // Email-only invitations (not yet linked)
        List<EventInvitation> emailInvitations = invitationRepository.findByEmailAndStatusAndInviteeIsNull(
                user.getEmail(), InvitationStatus.PENDING
        );

        // Merge without duplicates
        Map<Long, EventInvitation> merged = new LinkedHashMap<>();
        for (EventInvitation inv : invitations) merged.put(inv.getId(), inv);
        for (EventInvitation inv : emailInvitations) merged.put(inv.getId(), inv);

        return merged.values().stream().map(inv -> {
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", inv.getId());
            dto.put("eventId", inv.getEvent().getId());
            dto.put("eventTitle", inv.getEvent().getTitle());
            dto.put("invitedBy", inv.getInvitedBy() != null ? inv.getInvitedBy().getName() : "Unknown");
            dto.put("message", inv.getMessage());
            dto.put("sentAt", inv.getSentAt());
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * Accept invitation
     * POST /api/invitations/{id}/accept
     */
    @PostMapping("/invitations/{id}/accept")
    public ResponseEntity<?> acceptInvitation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        EventInvitation invitation = invitationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        User user = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify this invitation is for current user
        if (invitation.getInvitee() != null && !invitation.getInvitee().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Not your invitation"));
        }
        if (invitation.getInvitee() == null && !invitation.getEmail().equalsIgnoreCase(user.getEmail())) {
            return ResponseEntity.status(403).body(Map.of("error", "Not your invitation"));
        }

        invitation.markAccepted();
        invitation.setInvitee(user); // Link registered user
        invitationRepository.save(invitation);

        // Add user to event participants
        Event event = invitation.getEvent();
        event.addParticipant(user);
        eventRepository.save(event);

        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * Decline invitation
     * POST /api/invitations/{id}/decline
     */
    @PostMapping("/invitations/{id}/decline")
    public ResponseEntity<?> declineInvitation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        EventInvitation invitation = invitationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        User user = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify this invitation is for current user
        if (invitation.getInvitee() != null && !invitation.getInvitee().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Not your invitation"));
        }
        if (invitation.getInvitee() == null && !invitation.getEmail().equalsIgnoreCase(user.getEmail())) {
            return ResponseEntity.status(403).body(Map.of("error", "Not your invitation"));
        }

        invitation.markDeclined();
        invitationRepository.save(invitation);

        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * Get all invitations for an event (for event creator)
     * GET /api/events/{eventId}/invitations
     */
    @GetMapping("/events/{eventId}/invitations")
    public List<Map<String, Object>> getEventInvitations(@PathVariable Long eventId) {
        List<EventInvitation> invitations = invitationRepository.findByEventId(eventId);
        return invitations.stream().map(inv -> {
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", inv.getId());
            dto.put("email", inv.getEmail());
            dto.put("status", inv.getStatus());
            dto.put("sentAt", inv.getSentAt());
            dto.put("respondedAt", inv.getRespondedAt());
            if (inv.getInvitee() != null) {
                dto.put("userName", inv.getInvitee().getName());
            }
            return dto;
        }).collect(Collectors.toList());
    }
}
