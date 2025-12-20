package ru.practicum.evm;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Hit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String app;
    private String uri;
    private String ip;
    private LocalDateTime timestamp;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setApp(String app) {
        this.app = app;
    }
}