package ru.practicum.ewm.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record NewUserRequest(

        @NotBlank(message = "Имя не должно быть пустым")
        String name,

        @Email(message = "Некорректный email")
        @NotBlank(message = "Email не может быть пустым")
        String email
) {
}
