package com.example.crudboard.post.repository;

import com.example.crudboard.post.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

    /*
    Spring Date JPA가 메서드 이름(findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase)을 읽고 의미를 해석한다.
    단어별로 보변
    findBy: 조회 쿼리를 만든다.
    Title: Post 엔티티의 title 필드
    Containing: SQL로 치면 LIKE %keyword%
    IgnoreCase: 대소문자 무시 비교
    Or: OR 조건
    content: Post 엔티티의 content 필드
    ContainingIgnoreCase: 동일하게 %keyword%, 대소문자 무시

    그래서 결과적으로 SQL/JPQL은 아래와 같은 느낌으로 만들어 진다.
    	where upper(title) like upper('%keyword%') or upper(content) like upper('%keyword%')
	•	order by created_at desc
	•	limit/offset 적용
     */
    Page<Post> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
            String titleKeyWord,
            String contentKeyWord,
            Pageable pageable
    );
}

/*
JpaRepository를 상속받는 리포지토리 인터페이스를 만들면
JPA가 자동으로 이 인터페이스의 구현체를 만들어 스프링 빈으로 등록해준다.
그 구현체 내부에는 crud 메서드가 자동으로 구현되어 있기 때문에
우리는 repository를 통한 DB접근 메서드를 직접 구현하지 않아도 된다.

JpaRepository는 Repository, CRUDRepository를 상속받고 있기 때문에
편하게 다양한 기능을 사용할 수 있다.

save(), findById(), findAll(), count(), delete()
 */
