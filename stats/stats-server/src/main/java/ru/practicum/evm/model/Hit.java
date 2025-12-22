package ru.practicum.evm.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "hit")
@NoArgsConstructor
@AllArgsConstructor
public class Hit {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hit_seq")
    @SequenceGenerator(name = "hit_seq", sequenceName = "hit_id_seq", allocationSize = 1)
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
}