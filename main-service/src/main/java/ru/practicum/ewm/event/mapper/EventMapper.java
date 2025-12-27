package ru.practicum.ewm.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.event.dto.EventDto;
import ru.practicum.ewm.event.dto.request.CreateEventDto;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.user.mapper.UserMapper;

@Mapper(uses = {LocationMapper.class, UserMapper.class})
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", constant = "PENDING")
    @Mapping(target = "date", source = "eventDate")
    @Mapping(target = "location", source = "location")
    Event toEvent(CreateEventDto createRequest);

    @Mapping(target = "eventDate", source = "date")
    EventDto toEventDto(Event event);
}
