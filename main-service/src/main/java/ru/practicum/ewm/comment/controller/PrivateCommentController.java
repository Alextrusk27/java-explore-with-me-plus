package ru.practicum.ewm.comment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.CreateCommentDto;
import ru.practicum.ewm.comment.dto.request.CreateCommentBody;
import ru.practicum.ewm.comment.service.CommentService;
import ru.practicum.ewm.sharing.constants.ApiPaths;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.Private.COMMENTS)
@Validated
@Slf4j
public class PrivateCommentController {
    private final CommentService service;

    @PostMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto create(@PathVariable @Positive Long userId,
                             @PathVariable @Positive Long eventId,
                             @RequestBody @Valid CreateCommentBody body) {

        log.info("PRIVATE: Create comment {} for event {} by user {}", body.text(), eventId, userId);
        CreateCommentDto dto = CreateCommentDto.of(userId, eventId, body);
        CommentDto result = service.create(dto);
        log.info("PRIVATE: Comment created with id {}", result.id());
        return result;
    }
}
