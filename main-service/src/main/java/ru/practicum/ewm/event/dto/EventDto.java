package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.model.State;
import ru.practicum.ewm.user.dto.UserDto;

import java.time.LocalDateTime;

public record EventDto(
        Long id,
        LocalDateTime createdOn,
        String title,
        String annotation,
        String description,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime eventDate,

        State state,
        Boolean paid,
        Integer participantLimit,
        Boolean requestModeration,
        CategoryDto category,
        UserDto initiator,
        Location location,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime publishedOn
) {
}
