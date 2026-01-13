package ru.practicum.ewm.comment.service;

import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.UpdateCommentDto;
import ru.practicum.ewm.comment.dto.params.CommentParams;

import java.util.List;

public interface CommentService {

    List<CommentDto> get(CommentParams params);

    void delete(Long id);

    CommentDto update(UpdateCommentDto dto);
}
