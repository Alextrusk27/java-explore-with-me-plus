package ru.practicum.evm.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.evm.service.HitSerice;
import ru.practicum.exploreWithMe.EndpointHitDto;


@RestController
@RequestMapping("/hit")
public class HitController {

    private final HitSerice hitSerice;

    public HitController(HitSerice hitSerice) {
        this.hitSerice = hitSerice;
    }

    @PostMapping
    public ResponseEntity<EndpointHitDto> saveHit(@RequestBody @Valid EndpointHitDto hitDto) {
        EndpointHitDto savedHit = hitSerice.saveHit(hitDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedHit);
    }
}
