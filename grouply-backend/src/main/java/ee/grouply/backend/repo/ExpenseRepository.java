package ee.grouply.backend.repo;

import ee.grouply.backend.domain.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByEventIdOrderByCreatedAtDesc(Long eventId);
}