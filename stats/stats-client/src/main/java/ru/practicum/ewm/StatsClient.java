package ru.practicum.ewm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Component
public class StatsClient {
    private final RestTemplate rest;

    @Autowired
    public StatsClient(RestTemplateBuilder builder, @Value("${ewm.server.url}") String serverUrl) {
        this.rest = builder
                .rootUri(serverUrl)
                .build();
    }

    public ResponseEntity<HitDto> createHit(CreateHitDto createDto) {
        HttpEntity<CreateHitDto> requestEntity = new HttpEntity<>(createDto, defaultHeaders());
        return rest.exchange(
                "/hit",
                HttpMethod.POST,
                requestEntity,
                HitDto.class);
    }

    public ResponseEntity<List<ViewStatsDto>> getStats(String start, String end, List<String> uris, Boolean unique) {
        URI uri = UriComponentsBuilder
                .fromPath("/stats")
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParam("uris", uris != null ? uris.toArray() : new String[0])
                .queryParam("unique", unique)
                .build()
                .toUri();

        HttpEntity<Void> requestEntity = new HttpEntity<>(defaultHeaders());

        return rest.exchange(
                uri,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<>() {
                });
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}
