package ru.practicum.ewm.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

public record NewCategoryDto(
        @Size(min = 1, max = 50)
        @NotBlank
        String name
) {
}
