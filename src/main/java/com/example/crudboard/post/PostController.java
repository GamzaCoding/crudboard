package com.example.crudboard.post;

import com.example.crudboard.post.dto.PostCreateRequest;
import com.example.crudboard.post.dto.PostResponse;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid PostCreateRequest request) {
        Long id = postService.create(request);
        return ResponseEntity.created(URI.create("/api/posts/" + id)).build();
    }

    @GetMapping("/{id}")
    public PostResponse get(@PathVariable Long id) {
        return postService.get(id);
    }
    // PostResponse라는 객체로 리턴하는 이유를 자세하게 확인 해야햔다.
    /*
    PostResponse라는 객체로 리턴하는데 왜 JSON이 되나?
    컨트롤러가 PostResponse 객체를 반환하면
    스프링 MVC가 HttpMessageConverter를 통해 Jackson이 PostResponse를 JSON으로 직렬화 한다.

    @RestController라서 리턴 객체가 응답 바디로 나가고, 스프링 MVC가 HttpMessageConverter를 통해 Jackson 라이브러리로 DTO를 JSON으로
    직렬화해 내려준다. 엔티티 대신 DTO를 쓰는 이유는 API 스펙과 DB 모델 분리, 보안, 그리고 직렬화/지연로딘 문제를 피하기 위해서 입니다.
    JSON이 이상하게 안 나가면 대부분 원인
    1. DTO가 아니라 엔티티를 반환해서 직렬화 문제가 터짐
    2. 날짜/타입 직렬화 문제(LocalDateTime 등)
    3. Content-Type/Accept 헤더 불일치 문제
     */

    /*
    직렬화(Serialization)는 메모리 안의 객체(자바 객체)를 전송/저장할 수 있는 형태(문자열이나 바이트)로 바꾸는 것
    반대로 그걸 다시 객체로 만드는 것은 역직렬화(Deserialization)
    자바 객체 -> JSON 문자열로 바꾸는 과정 = 직렬화
     */

    /*
    @PathVariable
    말 그대로 경로 변수 이다.
    리소스 경로에 식별자를 넣어서 동적으로 URL에 정보를 담을 수 있다.

    @RequestParam이랑 헷갈릴 수 있느데 이 차이를 명확하게 이해하자
    위 두 어노테이션은 모두 클라이언트 요청에서 값을 받아오는 기능을 하지만 둘의 역할이 너무나도 비슷해 보인다.
    하지만 사용 목적과 위치가 다르기 때문에 이 둘의 차이를 알아보자

    @PathVariable -> 리소스를 식별할때 주로 사용
    URL 구조에 식별자(id)가 포함된다. -> 고유 ID
    RESTful한 리소스 URL 설계에 적합하다.
    리소스를 고유하게 지정하고 싶을 때 사용한다.

    @RequestParam 사용 -> 옵션을 넘길 떄
    ex) /posts?page=2?size=10
    즉, 페이지 번호나 필터링 옵션처럼 부가적인 조건에 적합하다.
    여러 값을 넘기기 쉽다. 순서 상관 없이 전달 가능하다.

    특정 ID기반 상세 조회 -> @PathVariable
    여러 조건을 기반으로 조회 -> @RequestParam
     */
}
