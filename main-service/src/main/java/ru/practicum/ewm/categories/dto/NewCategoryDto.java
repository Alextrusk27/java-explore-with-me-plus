package ru.practicum.ewm.categories.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record NewCategoryDto(
        @Size(min = 1, max = 50)
        @NotBlank
        String name
) {
}
