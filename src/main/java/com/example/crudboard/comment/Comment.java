package com.example.crudboard.comment;

import com.example.crudboard.post.Post;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "comments")
@Getter
public class Comment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * @ManyToOne : 댓글 N개 -> 게시글 1개
     * 의미: Comment 여러 개가 하나의 Post에 매핑됨
     * DB 관점: contents 테이블에 post_id FK 컬럼이 있고, 그 값이 posts.id를 가리킴
     * 측 Post 1개는 Comment 여러 개를 가질 수 있고
     * Comment 1개는 반드시 Post 1개에 속한다.
     *
     * fetch = FetchType.LAZY: 지연 로딩(프록시)
     * 의미: 댓글을 조회할 때 post를 바로 DB에서 같이 가져오지 않고, 필요할 때(comment.getPost() 호출 시점) 가져오게 함.
     * 실제 동작: Comment를 조회하면 post 필드는 처음에 프록시(가체 객체)로 들어가 있고,
     * 트랜잭션 안에서 comment.getPost().getTitle() 같이 접근하는 순간에 추가 쿼리가 나옴
     * 왜 LAZY가 기본적으로 유리할까?
     * 댓글 목록(예: 20개)을 조회할 때 게시글은 전부 같은데도 매번 같이 조인해서 가져오면 리소스 낭비가 생긴다.
     * 특히 댓글 목록 화면에서 "게시글 정보"를 쓰지 않는다면, post를 굳이 로딩할 필요가 없음.
     * 트랜잭션 밖에서 comment.getPost()를 만지면 LazyInitializationException이 날 수 있음(세선이 닫혔는데 프록시 초기화 하려고 해서)
     * 해결 패턴: 서비스 계층에서 DTO로 필요한 것만 뽑아서 내려주기
     * 또는 fetch join/EntityGraph로 필요한 경우만 한번에 가져오기
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false, length = 1000)
    private String content;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Comment() {}

    public Comment(Post post, String content) {
        this.post = post;
        this.content = content;
    }

    public void update(String content) {
        this.content = content;
    }
}
