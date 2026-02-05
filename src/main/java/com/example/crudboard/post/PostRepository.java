package com.example.crudboard.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

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
