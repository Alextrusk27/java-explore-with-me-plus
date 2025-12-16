package ru.practicum.exploreWithMe;

public record ViewStatsDto(
        String app,
        String uri,
        Long hits
) {
}
