package com.example.crudboard.comment;

import com.example.crudboard.comment.dto.CommentCreateRequest;
import com.example.crudboard.comment.dto.CommentResponse;
import com.example.crudboard.comment.service.CommentCommandService;
import com.example.crudboard.comment.service.CommentQueryService;
import com.example.crudboard.global.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private final CommentQueryService commentQueryService;
    private final CommentCommandService commentCommandService;

    public CommentController(CommentQueryService commentQueryService, CommentCommandService commentCommandService) {
        this.commentQueryService = commentQueryService;
        this.commentCommandService = commentCommandService;
    }

    @Operation(summary = "댓글 목록 조회(페이징)")
    @GetMapping
    public PageResponse<CommentResponse> list(
            @PathVariable Long postId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Direction.DESC) Pageable pageable
            ) {
        return commentQueryService.list(postId, pageable);
    }

    @Operation(summary = "댓글 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse create(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request
            ) {
        return commentCommandService.create(postId, request);
    }

    @Operation(summary = "댓글 삭제")
    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        commentCommandService.delete(postId, commentId);
    }
}
