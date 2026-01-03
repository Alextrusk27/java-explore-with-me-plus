package ru.practicum.ewm.sharing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class EntityFinder {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

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
}
