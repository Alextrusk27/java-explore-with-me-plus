package ru.practicum.ewm.user.mapper;


import org.springframework.stereotype.Component;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.model.User;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public User toEntity(NewUserRequest request) {
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        return user;
    }
}
