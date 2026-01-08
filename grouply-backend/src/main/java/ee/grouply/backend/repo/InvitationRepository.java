package ee.grouply.backend.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ee.grouply.backend.entity.EventInvitation;
import ee.grouply.backend.entity.EventInvitation.InvitationStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<EventInvitation, Long> {

    List<EventInvitation> findByInviteeIdAndStatus(Long inviteeId, InvitationStatus status);

    List<EventInvitation> findByEmailAndStatus(String email, InvitationStatus status);

    List<EventInvitation> findByEmailAndStatusAndInviteeIsNull(String email, InvitationStatus status);

    Optional<EventInvitation> findByToken(String token);

    List<EventInvitation> findByEventId(Long eventId);

    boolean existsByEventIdAndInviteeIdAndStatus(Long eventId, Long inviteeId, InvitationStatus status);

    boolean existsByEventIdAndEmailAndStatus(Long eventId, String email, InvitationStatus status);
}
