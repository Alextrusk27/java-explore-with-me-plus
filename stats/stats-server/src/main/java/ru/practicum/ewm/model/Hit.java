package ru.practicum.ewm.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Hit {
    Long id;
    App app;
    String uri;
    String ip;
    LocalDateTime timestamp;
}