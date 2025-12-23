package ru.practicum.ewm.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.ViewStatsDto;
import ru.practicum.ewm.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcStatsRepository {
    private final NamedParameterJdbcOperations jdbc;

    public Hit save(Hit hit) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("app", hit.getApp().getValue())
                .addValue("uri", hit.getUri())
                .addValue("ip", hit.getIp())
                .addValue("timestamp", hit.getTimestamp());

        String sql = """
                INSERT INTO hits (app, uri, ip, timestamp)
                VALUES (:app, :uri, :ip::inet, :timestamp)
                """;

        jdbc.update(sql, params);
        return hit;
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("start", start)
                .addValue("end", end);

        StringBuilder sqlBuilder = new StringBuilder();

        if (unique) {
            sqlBuilder.append("""
                    SELECT app, uri, COUNT(DISTINCT ip) AS hits
                    FROM hits
                    WHERE timestamp BETWEEN :start AND :end
                    """);
        } else {
            sqlBuilder.append("""
                    SELECT app, uri, COUNT(*) AS hits
                    FROM hits
                    WHERE timestamp BETWEEN :start AND :end
                    """);
        }
        if (!uris.isEmpty()) {
            params.addValue("uris", uris);
            sqlBuilder.append(" AND uri IN (:uris)");
        }
        sqlBuilder.append(" GROUP BY app, uri");
        sqlBuilder.append(" ORDER BY hits DESC");
        String sql = sqlBuilder.toString();

        return jdbc.query(sql, params, (rs, rowNum) ->
                new ViewStatsDto(
                        rs.getString("app"),
                        rs.getString("uri"),
                        rs.getLong("hits")
                ));
    }
}