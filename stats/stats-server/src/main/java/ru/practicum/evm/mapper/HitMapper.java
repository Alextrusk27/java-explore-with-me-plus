package ru.practicum.evm.mapper;

import ru.practicum.evm.model.Hit;
import ru.practicum.exploreWithMe.EndpointHitDto;

public class HitMapper {

    public static Hit toEntity(EndpointHitDto dto) {
        return new Hit(
                null,
                dto.getApp(),
                dto.getUri(),
                dto.getIp(),
                dto.getTimestamp()
        );
    }
}