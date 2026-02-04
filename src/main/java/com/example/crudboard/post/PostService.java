package com.example.crudboard.post;

import com.example.crudboard.global.exception.PostNotFoundException;
import com.example.crudboard.post.dto.PageResponse;
import com.example.crudboard.post.dto.PostCreateRequest;
import com.example.crudboard.post.dto.PostResponse;
import com.example.crudboard.post.dto.PostUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
// 클래스에 붙이면 이 클래스의 모든 public메서드가 기본적으로 트랜잭션 적용
// @Transactional은 스프링 AOP 프록시 기반이라, 기본적으로 public 메서드가 호출될 때 적용됨
/*
"같은 클래스 내부에서 자기 메서드 호출"은 트랜잭션이 안 걸릴 수 있음
this.someMethod() 같은 내부 호출은 프록시를 안 거쳐서 트랜잭션이 안 먹는 경우가 있음/
 */

public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Long create(PostCreateRequest request) {
        Post post = new Post(request.title(), request.content());
        // 이 부분에서 save를 하고 id도 반환하는 건가? -> id를 반환하는게 아니라 엔티티 자체(Post 객체)를 반환한다.
        return postRepository.save(post).getId();
        /*
        JpaRepository의 save() 시그니처는 <S extend T> S save(S entity); 이다.
        즉, 저장한 엔티티 자체를 반환한다.(여기서 T는 Post)
         */
    }

    /*
    @Transactional(readOnly = true)
    "이 메서드는 DB를 읽기만 할 거다" 라는 힌트
    장점 : Hibernate가 변경감지(dirty checking) 같은 작업을 줄여서 좀 더 가벼움
     */
    /*
    dirty checking은 뭘까?
    트랜잭션 안에서 조회한 엔티티의 값이 바뀌었는지 Hibernate가 자동으로 감지해서, 커밋 시점에 UPDATE SQL을 알아서 날리는 기능
    언제 발생하나?
    영속 상태 엔티티가 트랜잭션 안에서 변경되면, 트랜잭션 커밋 직전에 Hibernate가 변경 여부를 체크하고,
    변경이 있으면 UPDATE를 실행한다.

    예를 들면 update 메서드에서 post.update(req.title(), req.content());
    여기까지 하고 save(post)를 호줄 하지 않아도 된다.
    왜 save를 안해도 DB가 바뀌는지?
    findById()로 가져온 post는 영속성 컨텍스트가 관리하는 객체(영속 상태)야
    Hibernate는 조회 시점의 원본 상태(스냅샷)를 기억해 둔다.
    커밋 직전에 현재 값고 스냅샷을 비교해서 달라지면
    UPDATE 쿼리를 바로 실행한다. 이게 dirty checking이다.

    dirty checking이 실제로 실행되는 타이밍
    보통 트랜잭션 커밋 직전에 일어남다.

    왜 @Transactional(readOnly = true)면 다 가벼워 지나?
    조회 메서드는 dirty checking이 불필요하다 -> 데이터를 조회만 하지 변경하지 않으니까!
    그래서 결과적으로 변경 감지 대상 관리 비용이 줄어든다.

    dirty checking이 안 되는 대표 상황 3개
    1. 트랜잭션 밖에서 엔티티를 바꾼 경우
        * 영속성 컨텍스트가 없으면 관리도 안 해서 UPDATE 안 나감
    2. 조회한 에티티가 영속 상태가 아닌 경우
        * 예 : detach된 객체, DTO로 받은 값 등
    3. readyOnly 트랜잭션에서 변경하려고 하는 경우
        * 설정/환경에 따라 반영이 안 된거나 의도치 않은 동작이 나올 수 있다.

    한문장 정리
    dirty checking은 트랜잭션 안에서 영속 상태 엔티티의 변경을 Hibernate가 감지해서
    커밋 시점에 UPDATE SQL을 자동으로 생성/실행하는 기능입니다.
    그래서 수정 로직에서 save를 다시 호출하지 않아도 변경이 반영될 수 있습니다.
    조회 전용 트랜잭션(readOnly)은 이런 변경 감지 비용을 줄이는 데 도움이 됩니다.
     */
    @Transactional(readOnly = true)
    public PostResponse get(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        return PostResponse.from(post);
    }

    /*
    JpaRepository는 findAll(Pageable pageable)을 기본적으로 제공한다.
    이 메서드 내부적으로 DB에 LIMIT/OFFSET + ORDER BY가 들어간 SQL을 만들어서 실행한다.
    ex) select * from posts
        order by created_at desc
        limit 5 offset 0;
     */
    /*
    Page는 뭐야?
    Page<T>는 페이지 결과 + 메타데이터를 가지고 있는 스프링이 제공하는 객체다.
    content, totalElements, totalPages 등등 여러 메타데이터를 페이지 결과와 같이 가지고 있다.
    왜냐하면 프론트에서 UI를 만들때 이런 메타데이터들이 필요하기 때문이다.
     */
    /*
    totalElements/totalPages는 어떻게 계산되는가?
    이 메타데이터를 만들려면 DB에서 "전체 글 수"가 필요하다. 그래서 JPA는 보통 페이징 요청을 받으면 쿼리를 2번 실행하는 경우가 많다.
    1. 실제 페이지 데이터 조회 쿼리
    2. 전체 개수 조회 쿼리(count)
    참고 : 이 때문에 Page는 편하지만 count 쿼리 비용이 있다는 점이 있다. 그게 부담될 때는 Slice를 쓰기도 한다.
     */
    /*
    findAll(pageable)의 결과는 Page<Post>다. 여기서 map을 통해서 Dto로 변경해주는 것.
    이때 Page에서 제공하는 map()은 content리스트에 있는 각 Post를 PostResponse로 변환해주고, 페이징 메타데이터는 그대로 유지해서 Page<PostResponse>를 만들어 준다
     */
    // Page<Post> -> Page<PostResponse> 이렇게 변환된 것이 val page로 되고
    // PageResponse.from을 통해 PageResponse<PostResponse>로 변환된다.
    @Transactional(readOnly = true)
    public PageResponse<PostResponse> list(Pageable pageable) {
        Page<PostResponse> page = postRepository.findAll(pageable)
                .map(PostResponse::from);
        return PageResponse.from(page);
    }

    /*
    왜 save()가 없는데도 DB가 바뀌나?
    findById()로 가져온 post는 트랜잭션 안에서 영속 상태
    값이 바뀌면 Hibernate가 dirty checking(변경감지)로 커밋 시 UPDATE 쿼리를 자동으로 수행
    조회 -> 엔티티 변경 -> 커밋 시 자동으로 UPDATE 쿼리 날림
     */
    public void update(Long id, PostUpdateRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        post.update(request.title(), request.content());
    }

    /*
    왜 existsById()로 먼저 확인하지?
    deleteById() 자체도 없는 id면 예외가 날 수 있는데, 우리는 "없는 글이면 404로 통일"하고 싶음, 그래서 삭제 전에 존재여부 확인, 없으면 우리가 만든 예외로 처리
     */
    /*
    삭제 로직 시
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        postRepository.delete(post);

        위와 같은 로직으로도 처리할 수 있지만 select *로 컬럼을 다 가져오니까 단순 존재확인(existsById)보다 조회 비용이 크다
        엔티티가 관계를 많이 갖고 있으면(연간관계) 설정에 따라 무거워질 수 있다.
        단, 삭제 전 "검증/권한" 같은 로직이 필요하다면, 엔티티를 가져오는 이 로직이 더 좋을 수 있다.
     */
    public void delete(Long id) {
        if (!postRepository.existsById(id)) {
            throw new PostNotFoundException(id);
        }
        postRepository.deleteById(id);
    }
}
