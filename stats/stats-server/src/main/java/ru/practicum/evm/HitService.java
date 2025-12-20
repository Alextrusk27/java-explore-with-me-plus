package ru.practicum.evm;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exploreWithMe.EndpointHitDto;
import ru.practicum.exploreWithMe.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class HitService {

    private final HitRepository repository;

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (unique) {
            return repository.getUniqueHits(start, end, uris);
        } else {
            return repository.getAllHits(start, end, uris);
        }
    }

    public Hit saveHit(EndpointHitDto dto) {
        Hit hit = new Hit();
        hit.setApp(dto.app());
        hit.setUri(dto.uri());
        hit.setIp(dto.ip());
        hit.setTimestamp(dto.timestamp());
        return repository.save(hit);
    }

}
