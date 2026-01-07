package ru.practicum.ewm.compilations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record NewCompilationDto(
        List<Long> events,

        boolean pinned,

        @NotBlank
        @Size(min = 1, max = 50)
        String title
) {
}
