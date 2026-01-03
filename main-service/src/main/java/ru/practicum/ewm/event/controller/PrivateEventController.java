package ru.practicum.ewm.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventDto;
import ru.practicum.ewm.event.dto.EventDtoExtended;
import ru.practicum.ewm.event.dto.params.EventParams;
import ru.practicum.ewm.event.dto.params.EventParamsSorted;
import ru.practicum.ewm.event.dto.projection.EventInfo;
import ru.practicum.ewm.event.dto.request.CreateEventBody;
import ru.practicum.ewm.event.dto.request.CreateEventDto;
import ru.practicum.ewm.event.dto.request.UpdateEventBody;
import ru.practicum.ewm.event.dto.request.UpdateEventDto;
import ru.practicum.ewm.event.service.EventServiceImpl;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.service.RequestService;
import ru.practicum.ewm.sharing.constants.ApiPaths;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.Private.EVENTS)
@Validated
@RequiredArgsConstructor
@Slf4j
public class PrivateEventController {
    private final EventServiceImpl service;
    private final RequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto createEvent(
            @PathVariable @Positive Long userId,
            @RequestBody @Valid CreateEventBody body) {
        log.debug("Create event request: {} for user ID {}", body, userId);
        CreateEventDto dto = CreateEventDto.of(body, userId);
        EventDto result = service.createEvent(dto);
        log.debug("Created event ID {} for user ID {}", result.id(), userId);
        return result;
    }

    @GetMapping
    public List<EventInfo> getEvents(
            @PathVariable @Positive Long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.debug("Get events for user ID {} with params: from={}, size={}", userId, from, size);
        EventParamsSorted params = EventParamsSorted.of(userId, from, size);
        Page<EventInfo> result = service.getEvents(params);
        log.debug("Found {} events for user ID {}", result.getNumberOfElements(), userId);
        return result.getContent();
    }

    @GetMapping("/{eventId}")
    public EventDtoExtended getEvent(@PathVariable @Positive Long userId,
                                     @PathVariable @Positive Long eventId) {
        log.debug("Get event ID {} for user ID {}", eventId, userId);
        EventParams params = EventParams.of(userId, eventId);
        EventDtoExtended result = service.getEvent(params);
        log.debug("Found event ID {} for user ID {}", eventId, userId);
        return result;
    }

    @PatchMapping("/{eventId}")
    public EventDto updateEvent(@PathVariable @Positive Long userId,
                                @PathVariable @Positive Long eventId,
                                @RequestBody @Valid UpdateEventBody body) {
        log.debug("Update event ID {} for user ID {}", eventId, userId);
        UpdateEventDto dto = UpdateEventDto.of(body, userId, eventId);
        EventDto result = service.updateEvent(dto);
        log.debug("Updated event ID {} for user ID {}", eventId, userId);
        return result;
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getEventRequestsByUser(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId) {

        log.debug("Get participation requests for event ID {} by user ID {}", eventId, userId);
        return requestService.getEventParticipationRequests(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateEventRequestsByUser(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId,
            @RequestBody @Valid EventRequestStatusUpdateRequest updateRequest) {

        log.debug("Update requests status for event ID {} by user ID {}: {}", eventId, userId, updateRequest);
        return requestService.updateEventRequestStatus(userId, eventId, updateRequest);
    }
}
