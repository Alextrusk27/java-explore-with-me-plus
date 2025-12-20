//package ru.practicum.ewm;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestClient;
//
//@Service
//public class StatsClient {
//
//    private final RestClient restClient;
//
//    public StatsClient(@Value("${ewm.server.url}") String serverUrl) {
//        this.restClient = RestClient.builder()
//                .baseUrl(serverUrl)
//                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
//                .build();
//    }
//
//    public Object getStat(String start, String end, String[] uris, boolean isApiUnique) {
//        return restClient.get()
//                .uri(uriBuilder -> uriBuilder
//                        .path("/stats")
//                        .queryParam("start", start)
//                        .queryParam("end", end)
//                        .queryParam("uris", (Object[]) uris)
//                        .queryParam("unique", isApiUnique)
//                        .queryParam("format", "json")
//                        .build())
//                .retrieve()
//                .body(Object.class);
//    }
//}
