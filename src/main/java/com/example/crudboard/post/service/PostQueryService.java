package com.example.crudboard.post.service;

import com.example.crudboard.global.dto.PageResponse;
import com.example.crudboard.global.exception.PostNotFoundException;
import com.example.crudboard.post.Post;
import com.example.crudboard.post.PostRepository;
import com.example.crudboard.post.dto.PostResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class PostQueryService {

    private static final int MAX_SIZE = 50;

    private final PostRepository postRepository;

    public PostQueryService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public PostResponse get(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        return PostResponse.from(post);
    }

    public PageResponse<PostResponse> list(String keyword, Pageable pageable) {
        int size = Math.max(pageable.getPageSize(), MAX_SIZE);
        Pageable safePageable = PageRequest.of(pageable.getPageNumber(), size, pageable.getSort());

        String trimKeyword = (keyword == null) ? null : keyword.trim();
        var page = StringUtils.hasText(trimKeyword) ?
                postRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(trimKeyword, trimKeyword, safePageable).map(PostResponse::from)
                :
                postRepository.findAll(safePageable).map(PostResponse::from);
        return PageResponse.from(page);
    }
}
