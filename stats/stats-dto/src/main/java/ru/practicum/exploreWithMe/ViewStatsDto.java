package ru.practicum.exploreWithMe;

public record ViewStatsDto(
        String app,
        String uri,
        Long hits
) {
    public ViewStatsDto(String app, String uri, Long hits) {
        this.app = app;
        this.uri = uri;
        this.hits = hits == null ? 0L : hits;
    }
}
