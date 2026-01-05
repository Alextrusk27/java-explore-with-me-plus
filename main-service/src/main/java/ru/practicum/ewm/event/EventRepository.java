package ru.practicum.ewm.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.event.model.Event;

public interface EventRepository extends JpaRepository<Event, Long> {

    <T> Page<T> findByInitiatorId(Long initiatorId, Pageable pageable, Class<T> type);
}
