package ru.practicum.ewm.event.service;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.StatsClient;
import ru.practicum.ewm.ViewStatsDto;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.event.dto.EventDto;
import ru.practicum.ewm.event.dto.EventDtoExtended;
import ru.practicum.ewm.event.dto.params.EventParams;
import ru.practicum.ewm.event.dto.params.EventParamsSorted;
import ru.practicum.ewm.event.dto.params.EventSearchParams;
import ru.practicum.ewm.event.dto.projection.EventInfo;
import ru.practicum.ewm.event.dto.request.CreateEventDto;
import ru.practicum.ewm.event.dto.request.UpdateEventDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.State;
import ru.practicum.ewm.event.model.StateAction;
import ru.practicum.ewm.event.repository.EventPredicateBuilder;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.sharing.EntityFinder;
import ru.practicum.ewm.sharing.EntityValidator;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.ewm.sharing.constants.AppConstants.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;

    private final EntityFinder finder;
    private final EntityValidator validator;

    private final EventMapper mapper;

    private final StatsClient statsClient;

    @Override
    @Transactional
    public EventDto createEvent(CreateEventDto dto) {
        User initiator = finder.findUserOrThrow(dto.userId());
        Category category = finder.findCategoryOrThrow(dto.category());

        Event newEvent = mapper.toEntity(dto);
        newEvent.setCategory(category);
        newEvent.setInitiator(initiator);

        Event savedEvent = eventRepository.save(newEvent);

        return mapper.toDto(savedEvent);
    }

    @Transactional
    @Override
    public EventDto updateEvent(UpdateEventDto dto) {
        if (dto.userId() != null) {
            validator.validateUserExists(dto.userId());
        }

        Event event = finder.findEventOrThrow(dto.eventId());

        mapper.updateEntity(dto, event);

        if (categoryChanged(event, dto)) {
            Category category = finder.findCategoryOrThrow(dto.category());
            event.setCategory(category);
        }

        if (dto.hasStateAction()) {
            applyStateAction(event, dto.stateAction());
        }

        Event updatedEvent = eventRepository.save(event);

        return mapper.toDto(updatedEvent);
    }

    @Override
    public Page<EventInfo> getEvents(EventParamsSorted params) {
        validator.validateUserExists(params.userId());

        return eventRepository.findByInitiatorId(
                params.userId(),
                params.pageable(),
                EventInfo.class
        );
    }

    @Override
    public EventDtoExtended getEvent(EventParams params) {
        validator.validateUserExists(params.userId());

        Event event = finder.findEventOrThrow(params.eventId());
        long views = getStat(params.eventId());
        long confirmedRequests = requestRepository.countByEventIdAndStatus(params.eventId(), RequestStatus.CONFIRMED);

        return mapper.toExtendedDto(event, views, confirmedRequests);
    }

    @Override
    public List<EventDtoExtended> getEvents(EventSearchParams params) {
        Predicate predicate = new EventPredicateBuilder()
                .withInitiators(params.users())
                .withStates(params.states())
                .withCategories(params.categories())
                .withDateRange(params.rangeStart(), params.rangeEnd())
                .build();

        List<Event> events = eventRepository.findAll(predicate, EVENTS_DEFAULT_PAGEABLE)
                .getContent();

        if (events.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> views = getStat(events);

        List<Long> eventsIds = events.stream()
                .map(Event::getId)
                .toList();

        Map<Long, Long> confirmedRequests = requestRepository.getConfirmedRequestsCounts(eventsIds);

        return events.stream()
                .map(event -> mapper.toExtendedDto(
                        event,
                        views.getOrDefault(event.getId(), 0L),
                        confirmedRequests.getOrDefault(event.getId(), 0L)))
                .toList();
    }

    private Map<Long, Long> getStat(List<Event> events) {
        List<String> uris = events.stream()
                .map(p -> "event/" + p.getId())
                .toList();

        List<ViewStatsDto> stats = statsClient.getStats(
                        NIN_DATA_TIME,
                        MAX_DATA_TIME,
                        uris,
                        false)
                .getBody();

        if (stats == null) {
            return Collections.emptyMap();
        }

        return stats.stream().collect(Collectors.toMap(
                view -> extractIdFromUri(view.uri()),
                ViewStatsDto::hits
        ));
    }

    private Long getStat(Long eventId) {
        String uri = createUri(eventId);

        ResponseEntity<List<ViewStatsDto>> response = statsClient.getStats(
                NIN_DATA_TIME,
                MAX_DATA_TIME,
                List.of(uri),
                false
        );

        if (hasInvalidResponse(response)) {
            return 0L;
        }

        return Objects.requireNonNull(response.getBody()).stream()
                .mapToLong(ViewStatsDto::hits)
                .sum();
    }

    private boolean hasValidResponse(ResponseEntity<List<ViewStatsDto>> response) {
        if (response == null) {
            return false;
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Response status code is {}", response.getStatusCode());
            return false;
        }

        List<ViewStatsDto> body = response.getBody();
        return body != null && !body.isEmpty();
    }

    private boolean hasInvalidResponse(ResponseEntity<List<ViewStatsDto>> response) {
        return !hasValidResponse(response);
    }

    private String createUri(Long eventId) {
        return "event/" + eventId;
    }

    private Long extractIdFromUri(String uri) {
        String[] split = uri.split("/");
        return Long.parseLong(split[1]);
    }

    private boolean categoryChanged(Event event, UpdateEventDto dto) {
        Long dtoCategoryId = dto.category();

        if (dtoCategoryId == null) {
            return false;
        }

        Long eventCategoryId = event.getCategory().getId();
        return !eventCategoryId.equals(dtoCategoryId);
    }

    private void applyStateAction(Event event, StateAction stateAction) {
        switch (stateAction) {
            case PUBLISH_EVENT -> {
                if (!event.getState().equals(State.PUBLISHED)) {
                    event.setState(State.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                }
            }

            case CANCEL_REVIEW -> event.setState(State.CANCELED);

            default -> throw new IllegalArgumentException("Unacceptable state action: " + stateAction);
        }
    }
}