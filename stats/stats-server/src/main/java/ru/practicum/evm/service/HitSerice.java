package ru.practicum.evm.service;

import ru.practicum.exploreWithMe.EndpointHitDto;

public interface HitSerice {
    EndpointHitDto saveHit(EndpointHitDto dto);
}
