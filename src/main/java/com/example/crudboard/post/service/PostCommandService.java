package com.example.crudboard.post.service;

import com.example.crudboard.global.error.ApiException;
import com.example.crudboard.global.error.ErrorCode;
import com.example.crudboard.post.Post;
import com.example.crudboard.post.repository.PostRepository;
import com.example.crudboard.post.dto.PostCreateRequest;
import com.example.crudboard.post.dto.PostUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PostCommandService {

    private final PostRepository postRepository;

    public PostCommandService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Long create(PostCreateRequest request) {
        Post post = new Post(request.title(), request.content());
        return postRepository.save(post).getId();
    }

    public void update(Long id, PostUpdateRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));
        post.update(request.title(), request.content());
    }

    public void delete(Long id) {
        if (!postRepository.existsById(id)) {
            throw new ApiException(ErrorCode.POST_NOT_FOUND);
        }
        postRepository.deleteById(id);
    }
}
