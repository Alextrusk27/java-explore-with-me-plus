package ru.practicum.ewm.sharing.constants;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class AppConstants {

    public static final String MAIN_APP_NAME = "ewm-main-service";

    public static final String EVENTS_ROOT_PATH = "/events";
    public static final String EVENT_ROOT_PATH = "event/";

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

    public static final String NIN_DATA_TIME = LocalDateTime.of(2025, 1, 1, 0, 0, 0).format(DATE_TIME_FORMATTER);
    public static final String MAX_DATA_TIME = LocalDateTime.now().plusDays(1).format(DATE_TIME_FORMATTER);

    public static final Sort EVENTS_DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "eventDate");

    public static final Pageable REQUESTS_DEFAULT_PAGEABLE = PageRequest.of(0, 10, Sort.by("id"));
}
