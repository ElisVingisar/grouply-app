package ee.grouply.backend.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import ee.grouply.backend.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
}
