package com.example.crudboard.post;

import com.example.crudboard.global.exception.PostNotFoundException;
import com.example.crudboard.post.dto.PostCreateRequest;
import com.example.crudboard.post.dto.PostResponse;
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
        // 이 부분에서 save를 하고 id도 반환하는 건가?
        return postRepository.save(post).getId();
        /*
        JpaRepository의 save() 시그니처는 <S extend T> S save(S entity); 이다.
        즉, 저장한 엔티티 자체를 반환한다.(여기서 T는 Post)
         */
    }

    @Transactional(readOnly = true)
    public PostResponse get(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        return PostResponse.from(post);
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
}
