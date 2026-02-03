package com.example.crudboard.post;

import com.example.crudboard.post.dto.PostCreateRequest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

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
}
