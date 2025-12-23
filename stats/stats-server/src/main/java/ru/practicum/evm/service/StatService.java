package ru.practicum.evm.service;

import io.micrometer.core.instrument.config.validate.ValidationException;
import ru.practicum.exploreWithMe.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatService {
    List<ViewStatsDto> getStats(LocalDateTime start,
                                LocalDateTime end,
                                List<String> uris,
                                boolean unique) throws ValidationException;
}