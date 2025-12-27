package ru.practicum.ewm.event.service;

import ru.practicum.ewm.event.dto.EventDto;
import ru.practicum.ewm.event.dto.request.CreateEventDto;
import ru.practicum.ewm.event.dto.params.UserEventsParams;

import java.util.List;

public interface EventService {

    List<EventDto> getEvents(UserEventsParams params);

    EventDto createEvent(CreateEventDto createRequest);
}
