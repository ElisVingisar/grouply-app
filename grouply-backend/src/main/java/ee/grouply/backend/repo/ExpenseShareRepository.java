package ee.grouply.backend.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import ee.grouply.backend.entity.ExpenseShare;

public interface ExpenseShareRepository extends JpaRepository<ExpenseShare, Long> {
}