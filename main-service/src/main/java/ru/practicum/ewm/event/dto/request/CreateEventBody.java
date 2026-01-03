package ru.practicum.ewm.event.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import ru.practicum.ewm.sharing.annotation.AtLeastHoursFromNow;

import java.time.LocalDateTime;

public record CreateEventBody(

        @NotBlank(message = "Annotation required")
        @Size(max = 500, message = "Annotation must be no longer than 500 characters")
        String annotation,

        @NotNull(message = "Category required")
        @Positive(message = "Category ID must be > 0")
        Long category,

        @NotBlank(message = "Description required")
        @Size(max = 2000, message = "Description must be no longer than 2000 characters")
        String description,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @NotNull(message = "Event eventDate required")
        @AtLeastHoursFromNow(hours = 2)
        LocalDateTime eventDate,

        LocationBody location,

        @NotNull(message = "Paid option required")
        Boolean paid,

        @NotNull(message = "Participant limit required")
        @PositiveOrZero(message = "Participant limit cannot be < 0")
        Integer participantLimit,

        @NotNull(message = "Request moderation option required")
        Boolean requestModeration,

        @NotBlank(message = "Title required")
        @Size(max = 100, message = "Title must be no longer than 100 characters")
        String title
) {
}