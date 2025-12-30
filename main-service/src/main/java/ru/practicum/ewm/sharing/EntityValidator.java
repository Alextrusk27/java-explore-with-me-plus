package ru.practicum.ewm.sharing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.repository.UserRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class EntityValidator {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            log.warn("Validation failed: User with ID {} not found", userId);
            throw new NotFoundException("User with ID %d not found".formatted(userId));
        }
    }

    public void validateCategoryExists(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            log.warn("Validation failed: Category with ID {} not found", categoryId);
            throw new NotFoundException("Category with ID %d not found".formatted(categoryId));
        }
    }

    public void validateEventExists(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            log.warn("Validation failed: Event with ID {} not found", eventId);
            throw new NotFoundException("Event with ID %d not found".formatted(eventId));
        }
    }
}
