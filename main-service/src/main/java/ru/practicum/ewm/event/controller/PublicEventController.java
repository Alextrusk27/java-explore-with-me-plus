package ru.practicum.ewm.event.controller;

import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.sharing.constants.ApiPaths;

@RestController
@RequestMapping(ApiPaths.Public.EVENTS)
@Validated
public class PublicEventController {

    @GetMapping
    public Object getEvents() {
        return null;
    }

    @GetMapping("/{id}")
    public Object getEvent(@RequestParam @Positive Long id) {
        return null;
    }
}
