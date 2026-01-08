package ee.grouply.backend.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import ee.grouply.backend.entity.Expense;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByEventIdOrderByCreatedAtDesc(Long eventId);
}