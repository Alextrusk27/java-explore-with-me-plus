package ru.practicum.ewm.event.dto.params;

public record UserEventsParams(
        Long userId,
        Integer from,
        Integer size
) {
}
