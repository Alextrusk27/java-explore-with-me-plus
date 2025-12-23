package ru.practicum.evm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.evm.model.Hit;
import ru.practicum.exploreWithMe.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatRepository extends JpaRepository<Hit, Long> {

    @Query(value = """
                SELECT e.app, e.uri, COUNT(e.id) as hits
                FROM hit e
                WHERE e.timestamp BETWEEN :start AND :end
                  AND (:pattern IS NULL OR e.uri LIKE :pattern)
                GROUP BY e.app, e.uri
                ORDER BY hits DESC
            """, nativeQuery = true)
    List<Object[]> getStatsWithPatternNative(LocalDateTime start,
                                             LocalDateTime end,
                                             String pattern);

    @Query(value = """
                SELECT e.app, e.uri, COUNT(DISTINCT e.ip) as hits
                FROM hit e
                WHERE e.timestamp BETWEEN :start AND :end
                  AND (:pattern IS NULL OR e.uri LIKE :pattern)
                GROUP BY e.app, e.uri
                ORDER BY hits DESC
            """, nativeQuery = true)
    List<Object[]> getStatsUniqueWithPatternNative(LocalDateTime start,
                                                   LocalDateTime end,
                                                   String pattern);

    @Query("""
                SELECT new ru.practicum.exploreWithMe.ViewStatsDto(e.app, e.uri, COUNT(e))
                FROM Hit e
                WHERE e.timestamp BETWEEN :start AND :end
                GROUP BY e.app, e.uri
                ORDER BY COUNT(e) DESC
            """)
    List<ViewStatsDto> getStatsNoUri(LocalDateTime start, LocalDateTime end);

    @Query("""
                SELECT new ru.practicum.exploreWithMe.ViewStatsDto(e.app, e.uri, COUNT(DISTINCT e.ip))
                FROM Hit e
                WHERE e.timestamp BETWEEN :start AND :end
                GROUP BY e.app, e.uri
                ORDER BY COUNT(DISTINCT e.ip) DESC
            """)
    List<ViewStatsDto> getStatsUniqueNoUri(LocalDateTime start, LocalDateTime end);
}