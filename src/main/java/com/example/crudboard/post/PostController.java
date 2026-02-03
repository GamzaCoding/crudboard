package com.example.crudboard.post;

import com.example.crudboard.post.dto.PostCreateRequest;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid PostCreateRequest request) {
        Long id = postService.create(request);
        // 이부분에 대해서 자세한 설명이 필요할 듯
        return ResponseEntity.created(URI.create("/api/posts/" + id)).build();
    }

    /*
    ResponseEntity<T>는 스프링이 제공하는 HTTP응답을 내가 직접 조립하는 박스이다.
    여기에 담을 수 있는 것
    1. 상태코드(201, 404 등)
    2. 헤더(Location, Content-Type 등)
    3. 바디(JSON 데이터 등)

    위 create 메서드 시그니처가 ResponseEntity<Void>인 이유는 응답 바디를 비울 거라서 void인 것이다.
     */

    /*
    @RestController에서 객체를 리턴하면 스프링이
    1. 이건 응답 바디로 내보내야 겠다 라고 판단.
    2. HttpMessageConverter를 통해 변환
    3. 기본적으로 Jackson이 객체에서 JSON으로 직렬화 해줌
    그래서 PostResponse 같은 record를 리턴하면 자동으로 JSON이 됨
     */
}
