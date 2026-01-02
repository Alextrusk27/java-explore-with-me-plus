package ru.practicum.ewm.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.StatsClient;
import ru.practicum.ewm.ViewStatsDto;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.event.dto.EventDto;
import ru.practicum.ewm.event.dto.EventDtoExtended;
import ru.practicum.ewm.event.dto.params.EventParams;
import ru.practicum.ewm.event.dto.params.EventParamsSorted;
import ru.practicum.ewm.event.dto.projection.EventInfo;
import ru.practicum.ewm.event.dto.request.CreateEventDto;
import ru.practicum.ewm.event.dto.request.UpdateEventDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.State;
import ru.practicum.ewm.event.model.StateAction;
import ru.practicum.ewm.sharing.EntityFinder;
import ru.practicum.ewm.sharing.EntityValidator;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.ewm.sharing.constants.AppConstants.MAX_DATA_TIME;
import static ru.practicum.ewm.sharing.constants.AppConstants.NIN_DATA_TIME;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EventServiceImpl implements EventService {
    private final EventRepository repository;
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

        Event savedEvent = repository.save(newEvent);
        return mapper.toDto(savedEvent);
    }

    @Override
    public EventDto updateEvent(UpdateEventDto dto) {
        validator.validateUserExists(dto.userId());
        Event event = finder.findEventOrThrow(dto.eventId());
        mapper.updateEntity(dto, event);

        if (categoryChanged(event, dto)) {
            Category category = finder.findCategoryOrThrow(dto.category());
            event.setCategory(category);
        }

        if (dto.hasStateAction()) {
            applyStateAction(event, dto.stateAction());
        }

        Event updatedEvent = repository.save(event);
        return mapper.toDto(updatedEvent);
    }

    @Override
    public Page<EventInfo> getEvents(EventParamsSorted params) {
        validator.validateUserExists(params.userId());

        return repository.findByInitiatorId(
                params.userId(),
                params.pageable(),
                EventInfo.class
        );
    }

    @Override
    public EventDtoExtended getEvent(EventParams params) {
        validator.validateUserExists(params.userId());
        Event event = finder.findEventOrThrow(params.eventId());
        Long views = getStat(params.eventId());
        return mapper.toExtendedDto(event, views, 10L);
    }

    private Map<Long, Long> getViewsMap(List<EventInfo> projections) {
        List<String> uris = projections.stream()
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

    private boolean categoryChanged (Event event, UpdateEventDto dto) {
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
