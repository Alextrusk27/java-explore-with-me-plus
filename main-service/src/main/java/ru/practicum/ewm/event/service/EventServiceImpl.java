package ru.practicum.ewm.event.service;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.CreateHitDto;
import ru.practicum.ewm.StatsClient;
import ru.practicum.ewm.ViewStatsDto;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.event.dto.EventDto;
import ru.practicum.ewm.event.dto.EventDtoExtended;
import ru.practicum.ewm.event.dto.EventDtoShort;
import ru.practicum.ewm.event.dto.params.EventParams;
import ru.practicum.ewm.event.dto.params.EventParamsSorted;
import ru.practicum.ewm.event.dto.params.AdminSearchParams;
import ru.practicum.ewm.event.dto.params.PublicSearchParams;
import ru.practicum.ewm.event.dto.projection.EventInfo;
import ru.practicum.ewm.event.dto.request.CreateEventDto;
import ru.practicum.ewm.event.dto.request.UpdateEventDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.State;
import ru.practicum.ewm.event.repository.EventPredicateBuilder;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.sharing.EntityFinder;
import ru.practicum.ewm.sharing.EntityValidator;
import ru.practicum.ewm.user.model.User;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.*;
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
    public EventDto create(CreateEventDto dto) {
        User initiator = finder.findUserOrThrow(dto.userId());
        Category category = finder.findCategoryOrThrow(dto.category());

        Event newEvent = mapper.toEntity(dto);
        newEvent.setCategory(category);
        newEvent.setInitiator(initiator);

        log.warn("ТУТ ПОКА ВСЕ ОК {}", newEvent.getPaid());
        Event savedEvent = eventRepository.save(newEvent);

        return mapper.toDto(savedEvent);
    }

    @Transactional
    @Override
    public EventDto update(UpdateEventDto dto) {

        validator.validateUserExists(dto.userId());

        Event event = finder.findEventOrThrow(dto.eventId());

        if (event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        mapper.updateEntity(dto, event);

        if (categoryChanged(event, dto)) {
            Category category = finder.findCategoryOrThrow(dto.category());
            event.setCategory(category);
        }

        if (dto.hasStateAction()) {
            if (dto.stateAction() == StateAction.PUBLISH_EVENT ||
                    (dto.stateAction() == StateAction.REJECT_EVENT)) {
                throw new IllegalArgumentException("Illegal private state action: %s".formatted(dto.stateAction()));
            }
            applyStateAction(event, dto.stateAction());
        }

        Event updatedEvent = eventRepository.save(event);

        return mapper.toDto(updatedEvent);
    }

    @Transactional
    @Override
    public EventDto adminUpdate(UpdateEventDto dto) {
        Event event = finder.findEventOrThrow(dto.eventId());
        mapper.updateEntity(dto, event);

        if (dto.hasStateAction()) {
            if ((dto.stateAction() == StateAction.PUBLISH_EVENT ||
                    dto.stateAction() == StateAction.REJECT_EVENT) &&
                    event.getState() != State.PENDING) {

                throw new ConflictException("Cannot publish/reject the event because it's not in the right state: %s"
                        .formatted(event.getState()));

            } if (dto.stateAction() == StateAction.SEND_TO_REVIEW ||
                    dto.stateAction() == StateAction.CANCEL_REVIEW) {
                throw new IllegalArgumentException("Illegal admin state action: %s".formatted(dto.stateAction()));
            }

            applyStateAction(event, dto.stateAction());
        }

        if (categoryChanged(event, dto)) {
            Category category = finder.findCategoryOrThrow(dto.category());
            event.setCategory(category);
        }

        Event updatedEvent = eventRepository.save(event);

        return mapper.toDto(updatedEvent);
    }

    @Override
    public EventDtoExtended get(Long id) {
        Event event = finder.findEventOrThrow(id);

        if (!event.getState().equals(State.PUBLISHED)) {
            throw new NotFoundException(String.format("Event with id=%d was not found", id));
        }

        createHit(createUri(id));

        Long views = getStat(id, true);
        Long confirmedRequests = requestRepository.countByEventIdAndStatus(id, RequestStatus.CONFIRMED);

        return mapper.toExtendedDto(event, views, confirmedRequests);
    }

    @Override
    public Page<EventInfo> get(EventParamsSorted params) {
        validator.validateUserExists(params.userId());

        return eventRepository.findByInitiatorId(
                params.userId(),
                params.pageable(),
                EventInfo.class
        );
    }

    @Override
    public EventDtoExtended get(EventParams params) {
        validator.validateUserExists(params.userId());

        Event event = finder.findEventOrThrow(params.eventId());
        long views = getStat(params.eventId(), true);
        long confirmedRequests = requestRepository.countByEventIdAndStatus(params.eventId(), RequestStatus.CONFIRMED);

        return mapper.toExtendedDto(event, views, confirmedRequests);
    }

    @Override
    public List<EventDtoExtended> get(AdminSearchParams params) {
        Predicate predicate = new EventPredicateBuilder()
                .withInitiators(params.users())
                .withStates(params.states())
                .withCategories(params.categories())
                .withDateRange(params.rangeStart(), params.rangeEnd())
                .build();

        List<Event> events = eventRepository.findAll(predicate, params.pageable())
                .getContent();

        if (events.isEmpty()) {
            return List.of();
        }

        List<Long> eventsIds = events.stream()
                .map(Event::getId)
                .toList();

        Map<Long, Long> confirmedRequests = requestRepository.getConfirmedRequestsCounts(eventsIds);

        Map<Long, Long> views = getStat(events);

        return events.stream()
                .map(event -> mapper.toExtendedDto(
                        event,
                        views.getOrDefault(event.getId(), 0L),
                        confirmedRequests.getOrDefault(event.getId(), 0L)))
                .toList();
    }

    @Override
    public List<EventDtoShort> get(PublicSearchParams params) {
        Predicate predicate = new EventPredicateBuilder()
                .withTextSearch(params.text())
                .withCategories(params.categories())
                .withPaid(params.paid())
                .withDateRange(params.rangeStart(), params.rangeEnd())
                .forPublicSearch()
                .build();

        List<Event> events = eventRepository.findAll(predicate, params.pageable())
                .getContent();

        createHit(EVENTS_ROOT_PATH);

        if (events.isEmpty()) {
            return List.of();
        }

        List<Long> eventsIds = events.stream()
                .map(Event::getId)
                .toList();

        Map<Long, Long> confirmedRequests = requestRepository.getConfirmedRequestsCounts(eventsIds);

        events = filterAvailableEvents(events, confirmedRequests);

        Map<Long, Long> views = getStat(events);

        if (params.sort().equals(Sort.VIEWS)) {
            events.sort(Comparator.comparing(
                    event -> views.getOrDefault(event.getId(), 0L),
                    Comparator.reverseOrder()
            ));
        }

        return events.stream()
                .map(event -> mapper.toDtoShort(
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

    private Long getStat(Long eventId, boolean unique) {
        String uri = createUri(eventId);

        ResponseEntity<List<ViewStatsDto>> response = statsClient.getStats(
                NIN_DATA_TIME,
                MAX_DATA_TIME,
                List.of(uri),
                unique
        );

        if (hasInvalidResponse(response)) {
            return 0L;
        }

        return Objects.requireNonNull(response.getBody()).stream()
                .mapToLong(ViewStatsDto::hits)
                .sum();
    }

    private void createHit(String uri) {
        try {
            CreateHitDto dto = new CreateHitDto(
                    MAIN_APP_NAME,
                    uri,
                    InetAddress.getLocalHost().getHostAddress(),
                    LocalDateTime.now());

            statsClient.createHit(dto);

        } catch (UnknownHostException e) {
            throw new RuntimeException("Error while creating hit.", e);
        }
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
        return EVENT_ROOT_PATH + eventId;
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
                event.setRequestModeration(false);
                event.setState(State.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }
            case REJECT_EVENT, CANCEL_REVIEW -> {
                event.setRequestModeration(false);
                event.setState(State.CANCELED);
            }

            case SEND_TO_REVIEW -> {
                event.setRequestModeration(true);
                event.setState(State.PENDING);
            }
            default -> throw new IllegalArgumentException("Unacceptable state action: " + stateAction);
        }
    }

    private List<Event> filterAvailableEvents(List<Event> events, Map<Long, Long> confirmedRequests) {
        return events.stream()
                .filter(event -> {
                    long requestsCount = confirmedRequests.getOrDefault(event.getId(), 0L);
                    return requestsCount < event.getParticipantLimit();
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }
}