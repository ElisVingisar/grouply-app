package ee.grouply.backend.repo;

import ee.grouply.backend.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByEventId(Long eventId);
}