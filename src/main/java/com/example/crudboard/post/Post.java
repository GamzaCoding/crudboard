package com.example.crudboard.post;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity // 이 클래스는 JPA가 관리하는 엔티티 라는 표시
@EntityListeners(AuditingEntityListener.class)
@Table(name = "posts")
@Getter
public class Post {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 PK 생성
    // DB가 insert할 때 id를 만들고, 그걸 JPA가 받아와서 채워준다.
    // IDENTITY는 insert가 실행되어야 id가 생기는 편
    // save() 과정에서 id가 세팅되는 게 일반적이라 save() 직후 getId()가 정상 동작함
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 2000)
    private String content;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Post(){}
    // JPA가 엔티티를 로팅할 때 힐요한 기본 생성자
    // 외부에서 함부로 쓰이지 않게 protected로 막는 게 관례
    /*
    JPA는 프록시 기술을 사용하는데, 프록시 기술을 쓸 때, JPA hibernate가 객체를 강제로 만들어야 하는데
    private로 생성자를 설정하면 막혀버리기 때문이다. 그래서 protected 생성자를 사용하는 편이다.

    lombok의 @NoArgsConstructor(access = AccessLevel.PROTECTED)를 엔티티 클래스 위에
    선언함으로 간단하게 생성자를 사용할 수 있다.

    그럼 왜 JPA의 엔티티에 기본 생성자가 필요할까?
    jpa는 데이터를 DB에서 조회해 온 뒤 객체를 생성할때 Reflection을 사용한다.
    그런데 Reflection은 생성자의 매개변수 정보를 가져올 수 없다.
    그래서 기본 생성자가 존재하지 않는다면 DB에서 조회해 온 값을 엔티티로 만들 때 객체 생성자체가 실패하기 때문에
    , JPA에서는 기본스펙으로 기본 생성자를 반드시 생성해 줄 것을 정해놓고 있는 것이다.

    왜 Reflection을 사용할까?
    이는 우리가 엔티티로 어떤 타입을 생성할지 JPA는 알 수 없기 때문이다.
    Reflection을 사용하지 않고 객체를 생성하려면 미리 객체의 타입을 알고 있어야 한다.
    하지만 프레임워크나 라이브러리는 사용자가 정의할 구체 클래스 정보를 알 수가 없다.
    때문에 어떤 타입으로 엔티티를 만들더라도
    해당 엔티티를 생성하기 위해 Reflection을 사용하여 엔티티 인스턴스를 만들어 주는 것이다.
     */
    public Post(String title, String content) {
        this.title = title;
        this.content = content;
    }

    /*
    단순 setter 대신 "수정"이라는 행위를 엔티티에 캡슐화 -> 근데 setter 대신에 객체에 메시지를 던지라고 하지 않았나?
    답변 : update는 setter가 아니다!
    현재는 단순하게 맴버변수에 값을 대입하는 것으로 setter처럼 보일 수 있지만, 이 안에 검증로직 등 확장가능성이 있고,
    update라는 의미로 객체 메서드를 통해 캡슐화를 했다고 볼 수 있다.
     */
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}

