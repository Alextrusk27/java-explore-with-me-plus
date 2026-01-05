package ru.practicum.ewm.event.dto.params;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.event.dto.Sort;
import ru.practicum.ewm.event.model.State;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.ewm.sharing.constants.AppConstants.*;

public record PublicSearchParams(
        String text,
        List<Long> categories,
        Boolean paid,
        LocalDateTime rangeStart,
        LocalDateTime rangeEnd,
        Boolean onlyAvailable,
        Sort sort,
        Pageable pageable
) {
    public static PublicSearchParams of(
            String text,
            List<Long> categories,
            Boolean paid,
            String rangeStart,
            String rangeEnd,
            Boolean onlyAvailable,
            Sort sort,
            Integer from,
            Integer size) {

        if (rangeStart == null) {
            rangeStart = NIN_DATA_TIME;
        }

        if (rangeEnd == null) {
            rangeEnd = MAX_DATA_TIME;
        }

        Pageable pageable;

        if (sort.equals(Sort.EVENT_DATE)) {
            pageable = PageRequest.of(from / size, size, EVENTS_DEFAULT_SORT);
        } else {
            pageable = PageRequest.of(from / size, size);
        }

        return new PublicSearchParams(
                text,
                categories,
                paid,
                LocalDateTime.parse(rangeStart, DATE_TIME_FORMATTER),
                LocalDateTime.parse(rangeEnd, DATE_TIME_FORMATTER),
                onlyAvailable,
                sort,
                pageable
        );
    }
}
