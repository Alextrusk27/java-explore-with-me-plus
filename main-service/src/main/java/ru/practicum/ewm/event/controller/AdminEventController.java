package ru.practicum.ewm.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventDto;
import ru.practicum.ewm.event.dto.request.UpdateEventBody;
import ru.practicum.ewm.event.dto.request.UpdateEventDto;
import ru.practicum.ewm.event.service.EventServiceImpl;
import ru.practicum.ewm.sharing.constants.ApiPaths;

@RestController
@RequestMapping(ApiPaths.Admin.EVENTS)
@Validated
@RequiredArgsConstructor
@Slf4j
public class AdminEventController {

    private final EventServiceImpl service;

    @GetMapping
    public Object getEvents() {
        return null;
    }

    @PatchMapping("/{eventId}")
    public EventDto updateEvent(
            @PathVariable @Positive Long eventId,
            @RequestBody @Valid UpdateEventBody body) {

        UpdateEventDto dto = UpdateEventDto.of(body, null, eventId);
        return service.updateEvent(dto);
    }
}
