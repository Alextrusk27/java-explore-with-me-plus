package ru.practicum.ewm.event.controller;

import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.sharing.ApiPaths;

@RestController
@RequestMapping(ApiPaths.Admin.EVENTS)
@Validated
public class AdminEventController {

    @GetMapping
    public Object getEvents() {
        return null;
    }

    @PatchMapping("/{eventId}")
    public Object updateEvent(@PathVariable @Positive Long eventId) {
        return null;
    }
}
