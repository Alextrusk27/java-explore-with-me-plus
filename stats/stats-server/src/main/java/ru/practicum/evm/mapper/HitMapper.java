package ru.practicum.evm.mapper;

import ru.practicum.evm.model.Hit;
import ru.practicum.exploreWithMe.EndpointHitDto;

public class HitMapper {

    public static Hit toEntity(EndpointHitDto dto) {
        if (dto == null)
            return null;

        return Hit.builder()
                .app(dto.getApp())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .timestamp(dto.getTimestamp())
                .build();
    }
}