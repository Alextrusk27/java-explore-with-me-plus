package ru.practicum.ewm.event.service;

import org.springframework.data.domain.Page;
import ru.practicum.ewm.event.dto.EventDto;
import ru.practicum.ewm.event.dto.EventDtoExtended;
import ru.practicum.ewm.event.dto.params.EventParams;
import ru.practicum.ewm.event.dto.params.EventParamsSorted;
import ru.practicum.ewm.event.dto.params.EventSearchParams;
import ru.practicum.ewm.event.dto.projection.EventInfo;
import ru.practicum.ewm.event.dto.request.CreateEventDto;
import ru.practicum.ewm.event.dto.request.UpdateEventDto;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

import java.util.List;

public interface EventService {

    EventDto createEvent(CreateEventDto createRequest);

    EventDto updateEvent(UpdateEventDto updateRequest);

    Page<EventInfo> getEvents(EventParamsSorted params);

    EventDtoExtended getEvent(EventParams params);

    List<EventDtoExtended> getEvents(EventSearchParams params);
}
