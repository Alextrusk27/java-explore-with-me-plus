package ru.practicum.evm;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.exploreWithMe.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface HitRepository extends JpaRepository<Hit, Long> {

    @Query(value = """
            SELECT h.app AS app, h.uri AS uri, COUNT(h.ip) AS hits
            FROM hit h
            WHERE h.timestamp BETWEEN :start AND :end
              AND (:uris IS NULL OR h.uri IN (:uris))
            GROUP BY h.app, h.uri
            ORDER BY hits DESC
            """, nativeQuery = true)
    List<ViewStatsDto> getAllHits(@Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end,
                                  @Param("uris") List<String> uris);

    @Query(value = """
            SELECT h.app AS app, h.uri AS uri, COUNT(DISTINCT h.ip) AS hits
            FROM hit h
            WHERE h.timestamp BETWEEN :start AND :end
              AND (:uris IS NULL OR h.uri IN (:uris))
            GROUP BY h.app, h.uri
            ORDER BY hits DESC
            """, nativeQuery = true)
    List<ViewStatsDto> getUniqueHits(@Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end,
                                     @Param("uris") List<String> uris);
}
