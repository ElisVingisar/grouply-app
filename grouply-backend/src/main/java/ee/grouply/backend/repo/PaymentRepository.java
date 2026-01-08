package ee.grouply.backend.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import ee.grouply.backend.entity.Payment;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByEventId(Long eventId);
}