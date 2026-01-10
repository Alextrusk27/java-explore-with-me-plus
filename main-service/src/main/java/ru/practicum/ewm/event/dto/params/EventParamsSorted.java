package ru.practicum.ewm.event.dto.params;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static ru.practicum.ewm.sharing.constants.AppConstants.EVENTS_DEFAULT_SORT;

public record EventParamsSorted(
        Long userId,
        Pageable pageable
) {
    public static EventParamsSorted of(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size, EVENTS_DEFAULT_SORT);

        return new EventParamsSorted(
                userId,
                pageable
        );
    }
}
