package ru.practicum.ewm.event.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventDtoExtended;
import ru.practicum.ewm.event.dto.params.EventSearchParams;
import ru.practicum.ewm.event.model.State;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.sharing.constants.ApiPaths;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.Admin.EVENTS)
@Validated
@Slf4j
public class AdminEventController {
    private EventService service;

    @GetMapping
    public List<EventDtoExtended> getEvents(@RequestParam(required = false) @Positive List<Long> users,
                                            @RequestParam(required = false) List<State> states,
                                            @RequestParam(required = false) @Positive List<Long> categories,
                                            @RequestParam(required = false) String rangeStart,
                                            @RequestParam(required = false) String rangeEnd,
                                            @RequestParam(defaultValue = "0") Integer from,
                                            @RequestParam(defaultValue = "10") Integer size) {
        log.info("ADMIN: Get EVENTS with params: from={}, size={}", from, size);
        EventSearchParams params = EventSearchParams.of(users, states, categories, rangeStart, rangeEnd, from, size);
        List<EventDtoExtended> result = service.getEvents(params);
        log.info("ADMIN: Found {} EVENTS", result.size());
        return result;
    }

    @PatchMapping("/{eventId}")
    public Object updateEvent(@PathVariable @Positive Long eventId) {
        return null;
    }
}
