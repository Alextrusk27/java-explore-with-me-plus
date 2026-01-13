package ru.practicum.ewm.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.UpdateCommentDto;
import ru.practicum.ewm.comment.dto.params.CommentParams;
import ru.practicum.ewm.comment.mapper.CommentMapper;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.repository.CommentRepository;
import ru.practicum.ewm.sharing.BaseService;
import ru.practicum.ewm.sharing.EntityName;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends BaseService implements CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    @Override
    public List<CommentDto> get(CommentParams params) {
        List<Comment> comments = commentRepository.findByEventId(params.eventId(), params.pageable())
                .getContent();

        return comments.stream()
                .map(commentMapper::toDto)
                .toList();
    }

    @Override
    public void delete(Long id) {
        commentRepository.deleteById(id);
    }

    @Override
    public CommentDto update(UpdateCommentDto dto) {
        Comment comment = findCommentOrThrow(dto.commentId());
        commentMapper.updateEntity(dto, comment);
        Comment result = commentRepository.save(comment);
        return commentMapper.toDto(result);
    }

    private Comment findCommentOrThrow(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> throwNotFound(commentId, EntityName.COMMENT));
    }
}
