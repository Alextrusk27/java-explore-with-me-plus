package ru.practicum.evm.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.evm.exception.ValidationException;
import ru.practicum.evm.repository.StatRepository;
import ru.practicum.exploreWithMe.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class StatServiceImpl implements StatService {

    private final StatRepository repository;

    public StatServiceImpl(StatRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start,
                                       LocalDateTime end,
                                       List<String> uris,
                                       boolean unique) {
        log.info("получим запрос getStats: start={}, end={}, uris={}, unique={}", start, end, uris, unique);
        if (start.isAfter(end)) {
            throw new ValidationException("Начало не может быть позже конца");
        }
        if (uris == null || uris.isEmpty()) {
            return unique ? repository.getStatsUniqueNoUri(start, end)
                    : repository.getStatsNoUri(start, end);
        }
        String pattern = uris.getFirst() + "%";
        List<Object[]> rawResults = unique
                ? repository.getStatsUniqueWithPatternNative(start, end, pattern)
                : repository.getStatsWithPatternNative(start, end, pattern);
        List<ViewStatsDto> result = rawResults.stream()
                .map(row -> new ViewStatsDto(
                        (String) row[0],
                        (String) row[1],
                        ((Number) row[2]).longValue()
                ))
                .toList();
        log.info("результат запроса: {}", result);
        return result;
    }
}