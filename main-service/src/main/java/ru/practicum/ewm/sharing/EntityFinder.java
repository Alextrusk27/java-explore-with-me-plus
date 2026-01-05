package ru.practicum.ewm.sharing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class EntityFinder {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;

    public User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Searching failed: User with ID {} not found", userId);
                    return new NotFoundException(
                            "User with ID %d not found".formatted(userId)
                    );
                });
    }

    public Category findCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("Searching failed: Category with ID {} not found", categoryId);
                    return new NotFoundException(
                            "Category with ID %d not found".formatted(categoryId)
                    );
                });
    }

    public Event findEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("Searching failed: Event with ID {} not found", eventId);
                    return new NotFoundException(
                            "Event with ID %d not found".formatted(eventId));
                });
    }

    public ParticipationRequest findEventRequestOrThrow(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.warn("Searching failed: EventRequest with ID {} not found", requestId);
                    return new NotFoundException(
                            "EventRequest with ID %d not found".formatted(requestId)
                    );
                });
    }
}
