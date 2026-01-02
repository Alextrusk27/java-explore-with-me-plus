package ru.practicum.ewm.sharing.constants;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class AppConstants {

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
    public static final String NIN_DATA_TIME = LocalDateTime.of(2025, 1, 1, 0, 0, 0).format(DATE_TIME_FORMATTER);
    public static final String MAX_DATA_TIME = LocalDateTime.now().plusDays(1).format(DATE_TIME_FORMATTER);
}
