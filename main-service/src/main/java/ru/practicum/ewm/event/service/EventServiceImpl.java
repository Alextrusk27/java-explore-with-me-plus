package ru.practicum.ewm.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.event.dto.EventDto;
import ru.practicum.ewm.event.dto.request.CreateEventDto;
import ru.practicum.ewm.event.dto.params.UserEventsParams;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;

    @Override
    @Transactional
    public EventDto createEvent(CreateEventDto createRequest) {
        User initiator = getUserOrThrow(createRequest.userId());
        Category category = getCategoryOrThrow(createRequest.category());

        Event newEvent = eventMapper.toEvent(createRequest);

        newEvent.setCategory(category);
        newEvent.setInitiator(initiator);

        Event savedEvent = eventRepository.save(newEvent);

        return eventMapper.toEventDto(savedEvent);
    }

    @Override
    public List<EventDto> getEvents(UserEventsParams params) {
        return List.of();
    }

    private User getUserOrThrow(Long userId) {
        log.warn("Event service: User with ID {} Not Found", userId);
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("User with ID %d not found".formatted(userId)));
    }

    private Category getCategoryOrThrow(Long categoryId) {
        log.warn("Event service: Category with ID {} Not Found", categoryId);
        return categoryRepository.findById(categoryId).orElseThrow(() ->
                new NotFoundException("Category with ID %d not found".formatted(categoryId)));
    }
}
