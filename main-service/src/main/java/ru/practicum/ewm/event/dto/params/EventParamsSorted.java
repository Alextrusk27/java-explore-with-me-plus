package ru.practicum.ewm.event.dto.params;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record EventParamsSorted(
        Long userId,
        Pageable pageable
) {
    public static EventParamsSorted of(Long userId, Integer from, Integer size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "eventDate");
        Pageable pageable = PageRequest.of(from / size, size, sort);

        return new EventParamsSorted(
                userId,
                pageable
        );
    }
}
