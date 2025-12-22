package ru.practicum.evm.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.evm.service.StatService;
import ru.practicum.exploreWithMe.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class StatController {

    private final StatService statsService;
    private static final String DATE_TIME = "yyyy-MM-dd HH:mm:ss";

    public StatController(StatService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(@RequestParam
                                       @DateTimeFormat(pattern = DATE_TIME)
                                       LocalDateTime start,
                                       @RequestParam
                                       @DateTimeFormat(pattern = DATE_TIME)
                                       LocalDateTime end,
                                       @RequestParam(required = false) List<String> uris,
                                       @RequestParam(defaultValue = "false") Boolean unique) {
        return statsService.getStats(start, end, uris, unique);
    }
}