package ru.practicum.ewm.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.CreateCommentDto;
import ru.practicum.ewm.comment.dto.PrivateUpdateCommentDto;
import ru.practicum.ewm.comment.dto.params.CommentParams;
import ru.practicum.ewm.comment.dto.request.CreateCommentBody;
import ru.practicum.ewm.comment.dto.request.UpdateCommentBody;
import ru.practicum.ewm.comment.service.CommentService;
import ru.practicum.ewm.sharing.constants.ApiPaths;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.Private.COMMENTS)
@Validated
@Slf4j
public class PrivateCommentController {
    private final CommentService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(
            @PathVariable Long userId,
            @RequestParam Long eventId,
            @Valid @RequestBody CreateCommentBody body
    ) {
        log.info("PRIVATE: Юзер {} создал комментарий для события {}: {}", userId, eventId, body.text());
        return service.create(CreateCommentDto.of(userId, eventId, body));
    }

    @GetMapping
    public List<CommentDto> getComments(
            @PathVariable Long userId,
            @RequestParam Long eventId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("PRIVATE: Юзер {} получил комментарий по эвенту {} from {} size {}", userId, eventId, from, size);
        CommentParams params = CommentParams.of(eventId, from, size);
        return service.get(params);
    }

    @GetMapping("/{commentId}")
    public CommentDto getComment(
            @PathVariable Long userId,
            @PathVariable Long commentId
    ) {
        log.info("PRIVATE: Юзер {} получил комментарий {}", userId, commentId);
        return service.get(commentId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(
            @PathVariable Long userId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentBody body
    ) {
        log.info("PRIVATE: Юзер {} обновио свой комментраий {}", userId, commentId);
        return service.updatePrivate(PrivateUpdateCommentDto.of(userId, commentId, body));
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable Long userId,
            @PathVariable Long commentId
    ) {
        log.info("PRIVATE: юзер {} удалил свой комментарий {}", userId, commentId);
        service.deleteByUser(userId, commentId);
    }
}

