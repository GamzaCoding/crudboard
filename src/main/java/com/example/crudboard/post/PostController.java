package com.example.crudboard.post;

import com.example.crudboard.global.exception.ApiExceptionHandler.ErrorResponse;
import com.example.crudboard.global.dto.PageResponse;
import com.example.crudboard.post.dto.PostCreateRequest;
import com.example.crudboard.post.service.PostQueryService;
import com.example.crudboard.post.dto.PostResponse;
import com.example.crudboard.post.dto.PostUpdateRequest;
import com.example.crudboard.post.service.PostCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Posts", description = "게시글 CRUD API")
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostCommandService postCommandService;
    private final PostQueryService postQueryService;

    public PostController(PostCommandService postCommandService, PostQueryService postQueryService) {
        this.postCommandService = postCommandService;
        this.postQueryService = postQueryService;
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
    @Operation(summary = "게시글 생성")
    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid PostCreateRequest request) {
        Long id = postCommandService.create(request);
        return ResponseEntity.created(URI.create("/api/posts/" + id)).build();
    }

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
    @Operation(summary = "게시글 단건 조회", tags = {"Posts"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public PostResponse get(@PathVariable Long id) {
        return postQueryService.get(id);
    }

    /*
    Pageable이 뭐지?
    스프링 데이터에서 제공하는 "페이징 요청 정보"객체다. 클라이언트가 URL로 보낸 값을 스프링이 자동으로 파싱해서 만들어 준다.
    예) GET /api/posts?page=0&size=5&sort=createdAt, desc
    즉, 컨트롤러에서 Pageable을 파라미터로 받는 순간 요청이 원하는 페이지 조건이 이미 객체로 준비돼서 들어온다.
     */
//    @Operation(summary = "게시글 목록 조회(페이징)")
//    @GetMapping
//    public PageResponse<PostResponse> list(Pageable pageable) {
//        return LagacypostService.list(pageable);
//    }

    /*
    @RequestParam(required = false)는 keyword가 없어도 요청이 에러가 안 나고, keyword는 null로 들어온다.
    pageable은 ?Page=0&size=5&sort=createdAt,desc 같은 쿼리 스트링이 자동으로 파싱돼서 들어온다.
     */
//    @Operation(summary = "게시글 목록 조회(페이징)")
//    @GetMapping
//    public PageResponse<PostResponse> list(
//            @RequestParam(required = false) String keyword,
//            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
//        return postQueryService.list(keyword, pageable);
//    }

    @Operation(summary = "게시글 목록 조회(페이징 + 검색 조건)")
    @GetMapping
    public PageResponse<PostResponse> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "TITLE_CONTENT") PostSearchType type,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME)LocalDateTime createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME)LocalDateTime createdTo,
            @PageableDefault(size = 5, sort = "createdAt", direction = Direction.DESC) Pageable pageable
            ) {
        PostSearchCondition condition = new PostSearchCondition(keyword, type, createdFrom, createdTo);
        return postQueryService.list(condition, pageable);
    }


    @Operation(summary = "게시글 수정", tags = {"Posts"})
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id,
                                       @RequestBody @Valid PostUpdateRequest request){
        postCommandService.update(id, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "게시글 삭제", tags = {"Posts"})
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        postCommandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
