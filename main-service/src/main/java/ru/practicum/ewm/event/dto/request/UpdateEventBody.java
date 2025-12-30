package ru.practicum.ewm.event.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import ru.practicum.ewm.event.model.StateAction;
import ru.practicum.ewm.sharing.annotation.AtLeastHoursFromNow;

import java.time.LocalDateTime;

public record UpdateEventBody(
        @Size(max = 500, message = "Annotation must be no longer than 500 characters")
        String annotation,

        @Positive(message = "Category ID must be > 0")
        Long category,

        @Size(max = 2000, message = "Description must be no longer than 2000 characters")
        String description,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @AtLeastHoursFromNow(hours = 2)
        LocalDateTime eventDate,

        CreateLocationBody location,

        Boolean paid,

        @PositiveOrZero(message = "Participant limit cannot be < 0")
        Integer participantLimit,

        Boolean requestModeration,

        StateAction stateAction,

        @Size(max = 100, message = "Title must be no longer than 100 characters")
        String title
) {
}
