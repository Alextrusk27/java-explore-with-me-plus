package ru.practicum.evm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.evm.mapper.HitMapper;
import ru.practicum.evm.repository.StatRepository;
import ru.practicum.exploreWithMe.EndpointHitDto;
import ru.practicum.exploreWithMe.ViewStatsDto;
import ru.practicum.evm.exception.ValidationException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatServiceImpl implements StatService {

    private final StatRepository repository;

    @Override
    public void saveHit(EndpointHitDto dto) {
        repository.save(HitMapper.toEntity(dto));
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start,
                                       LocalDateTime end,
                                       List<String> uris,
                                       boolean unique) {
        if (start.isAfter(end)) {
            throw new ValidationException("Yfxfkj hfymit xtv rjytw");
        }
        List<ViewStatsDto> result = (uris == null || uris.isEmpty())
                ? (unique ? repository.getStatsUniqueNoUri(start, end)
                : repository.getStatsNoUri(start, end))
                : (unique ? repository.getStatsUnique(start, end, uris)
                : repository.getStats(start, end, uris));
        return result;
    }
}