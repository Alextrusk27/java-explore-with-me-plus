package ru.practicum.ewm.comment.dto;

import ru.practicum.ewm.user.dto.UserShortDto;

import java.time.LocalDateTime;

public record CommentDto (
    Long id,
    String text,
    UserShortDto author,
    LocalDateTime createdOn,
    Long eventId
) {
}
