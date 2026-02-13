package com.example.crudboard.comment.service;

import com.example.crudboard.comment.Comment;
import com.example.crudboard.comment.dto.CommentCreateRequest;
import com.example.crudboard.comment.dto.CommentResponse;
import com.example.crudboard.comment.dto.CommentUpdateRequest;
import com.example.crudboard.comment.repository.CommentRepository;
import com.example.crudboard.global.error.ApiException;
import com.example.crudboard.global.error.ErrorCode;
import com.example.crudboard.post.Post;
import com.example.crudboard.post.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CommentCommandService {

    @Autowired // 이거 물어보자
    private final PostRepository postRepository;
    @Autowired
    private final CommentRepository commentRepository;

    public CommentCommandService(PostRepository postRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    public CommentResponse create(Long postId, CommentCreateRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

        Comment comment = new Comment(post, request.content());
        Comment savedComment = commentRepository.save(comment);

        return new CommentResponse(
                savedComment.getId(),
                postId,
                savedComment.getContent(),
                savedComment.getCreatedAt(),
                savedComment.getUpdatedAt()
        );
    }

    public CommentResponse update(Long postId, Long commentId, CommentUpdateRequest request) {
        // "해당 게시글의 댓글" 인지 보장
        if (!commentRepository.existsByIdAndPostId(commentId, postId)) {
            throw new ApiException(ErrorCode.COMMENT_NOT_FOUND);
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ApiException(ErrorCode.COMMENT_NOT_FOUND));

        comment.update(request.content());

        return new CommentResponse(
                comment.getId(),
                postId,
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }

    public void delete(Long postId, Long commentId) {
        if (!commentRepository.existsByIdAndPostId(commentId, postId)) {
            throw new ApiException(ErrorCode.COMMENT_NOT_FOUND);
        }
        commentRepository.deleteByIdAndPostId(commentId, postId);
    }
}
