package ru.practicum.ewm.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventDto;
import ru.practicum.ewm.event.dto.request.CreateEventBody;
import ru.practicum.ewm.event.dto.request.CreateEventDto;
import ru.practicum.ewm.event.dto.params.UserEventsParams;
import ru.practicum.ewm.event.service.EventServiceImpl;
import ru.practicum.ewm.sharing.ApiPaths;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.Private.EVENTS)
@Validated
@RequiredArgsConstructor
@Slf4j
public class PrivateEventController {
    private final EventServiceImpl eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto createEvent(@PathVariable @Positive Long userId,
                              @RequestBody @Valid CreateEventBody body) {
        log.debug("Create event request: {} for user ID {}", body, userId);
        CreateEventDto dto = CreateEventDto.of(body, userId);
        EventDto result = eventService.createEvent(dto);
        log.debug("Created event ID {} for user ID {}", result.id(), userId);
        return result;
    }

    @GetMapping
    public List<EventDto> getEvents(@PathVariable @Positive Long userId,
                                    @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                    @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.debug("Get events for user ID {} with params: from={}, size={}", userId, from, size);
        UserEventsParams params = new UserEventsParams(userId, from,size);
        List<EventDto> result = eventService.getEvents(params);
        log.debug("Found {} events for user ID {}", result.size(), userId);
        return result;
    }

    @GetMapping("/{eventId}")
    public Object getEvent(@PathVariable @Positive Long userId,
                           @PathVariable @Positive Long eventId) {
        return null;
    }

    @PatchMapping("/{eventId}")
    public Object updateEvent(@PathVariable @Positive Long userId,
                              @PathVariable @Positive Long eventId) {
        return null;
    }

    @GetMapping("/{eventId}/requests")
    public Object getEventRequestsByUser(@PathVariable @Positive Long userId,
                                         @PathVariable @Positive Long eventId) {
        return null;
    }

    @PatchMapping("/{eventId}/requests")
    public Object updateEventRequestsByUser(@PathVariable @Positive Long userId,
                                            @PathVariable @Positive Long eventId) {
        return null;
    }
}
