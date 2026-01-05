package ru.practicum.ewm.event.dto.params;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.event.model.State;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.ewm.sharing.constants.AppConstants.*;

public record AdminSearchParams(
        List<Long> users,
        List<State> states,
        List<Long> categories,
        LocalDateTime rangeStart,
        LocalDateTime rangeEnd,
        Pageable pageable
) {
    public static AdminSearchParams of(
            List<Long> users,
            List<State> states,
            List<Long> categories,
            String rangeStart,
            String rangeEnd,
            Integer from,
            Integer size) {

        Pageable pageable = PageRequest.of(from / size, size, EVENTS_DEFAULT_SORT);

        if (rangeStart == null) {
            rangeStart = NIN_DATA_TIME;
        }

        if (rangeEnd == null) {
            rangeEnd = MAX_DATA_TIME;
        }

        return new AdminSearchParams(
                users,
                states,
                categories,
                LocalDateTime.parse(rangeStart, DATE_TIME_FORMATTER),
                LocalDateTime.parse(rangeEnd, DATE_TIME_FORMATTER),
                pageable
        );
    }
}
