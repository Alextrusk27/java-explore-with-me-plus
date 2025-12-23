package ru.practicum.evm.service;

import org.springframework.stereotype.Service;
import ru.practicum.evm.exception.ValidationException;
import ru.practicum.evm.mapper.HitMapper;
import ru.practicum.evm.model.Hit;
import ru.practicum.evm.repository.StatRepository;
import ru.practicum.exploreWithMe.EndpointHitDto;

@Service
public class HitServiceImpl implements HitSerice {
    private final StatRepository repository;

    public HitServiceImpl(StatRepository repository) {
        this.repository = repository;
    }

    @Override
    public EndpointHitDto saveHit(EndpointHitDto dto) {
        if (dto == null) {
            throw new ValidationException("EndpointHitDto не может быть null");
        }
        Hit hit = HitMapper.toEntity(dto);
        if (hit == null) {
            throw new ValidationException("Не получается преобразовать DTO в сущность Hit");
        }
        Hit saved = repository.save(hit);
        return dto;
    }
}