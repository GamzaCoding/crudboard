package com.example.crudboard.comment.service;

import com.example.crudboard.comment.Comment;
import com.example.crudboard.comment.dto.CommentResponse;
import com.example.crudboard.comment.repository.CommentRepository;
import com.example.crudboard.global.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CommentQueryService {

    private final CommentRepository commentRepository;

    public CommentQueryService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public PageResponse<CommentResponse> list(Long postId, Pageable pageable) {
        Page<Comment> page = commentRepository.findByPostId(postId, pageable);

        return PageResponse.from(page.map(c ->
                        new CommentResponse(
                                c.getId(),
                                c.getPost().getId(),
                                c.getContent(),
                                c.getCreatedAt(),
                                c.getUpdatedAt()
                        )
        ));
    }
}
