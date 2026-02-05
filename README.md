# crudboard
SpringBoot 기반 CRUD 게시판 REST API 연습 프로젝트 입니다.
게시글(Post)에 대해 생성/조회/수정/삭제(CRUD)와 목록 조회(페이징), 예외 처리, 통합 테스트(MockMvc)를 구현했습니다.
---
## Tech Stack
* Java 21
* Spring Boot 4.0.2
* Spring Web(Spring MVC)
* Spring Data JPA(Hibernate)
* H2 Database(file / test: in-memory)
* Validation(jakarta validation)
* JUnit5, MockMvc
---

## Getting Started
1) Run
```bash
./gradlew bootRun
```
* Server:http://localhost:8080

2) H2 Console
* URL:http://localhost:8080/h2-console
* JDBC URL 예시: jdbc:h2:file:./data/crudboard;MODE=MySQL
* username:sa
* password:(empty)
---

## API Spec
Base URL:http://localhost:8080

### Create Post
* POST /api/posts
* Request Body
```json
{
  "title": "hello",
  "content": "first post"
}
```
* Response
  * 201 Created
  * Location: /api/posts/{id}

### Get Post
* GET /api/posts/{id}
* Response: 200 OK

```json
{
  "id": 1,
  "title": "hello",
  "content": "first post",
  "createdAt": "2026-02-03T20:05:27.098914",
  "updatedAt": "2026-02-03T20:05:27.098931"
}
```

### List Posts(Paging)
* GET /api/posts?page=0&size=5&sort=createAt,desc
* Response: 200 OK
```json
{
  "content": [
    {
      "id": 2,
      "title": "title2",
      "content": "content2",
      "createdAt": "2026-02-03T20:12:08.234895",
      "updatedAt": "2026-02-03T20:12:08.234913"
    }
  ],
  "totalElements": 2,
  "totalPages": 1,
  "size": 5,
  "number": 0,
  "first": true,
  "last": true
}
```

### Update Post
* PUT /api/posts/{id}
* Request Body
```json
{
  "title": "updated title",
  "content": "updated content"
}
```
* Response: 204 No Content

### Delete Post
* DELETE /api/posts/{id}
* Response: 204 No Content
---
## Error Response
### Post Not Found
* Response: 404 Not Found
```json
{
  "code": "POST_NOT_FOUND",
  "message": "Post not found. id=1",
  "details": null,
  "timestamp": "2026-02-04T14:57:49.437688"
}
```
---
## Tests
### Run Tests
```bash
./gradlew test
```
* MockMvc 기반 통합 테스트로 아래 시나리오를 검증합니다.
  * Create: 201 Created + Location 헤더
  * Read: 200 OK + 응답 JSON 검증
  * Not Found: 404 + 에러 코드 검증
  * Update: 204 + 수정 반영 확인
  * Delete: 204 + 삭제 후 404 확인
---
## Project Structure
* post
  * Post 
  * PostController
  * PostService
  * PostRepository
  * dto(PostCreateRequest, PostUpdateRequest, PostResponse)
* global.exception
  * ApiExceptionHandler
  * PostNotFoundException
---

## 트러블 슈팅
### H2 컬럼 불일치 이슈

#### 기존 상황
H2 DB를 file로 사용 중임으로 서버를 껐다가 켜도 이전 테이블 구조가 남아 있다.
```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/crudboard;MODE=MySQL
```

#### 문제 상황 발생
개발 도중 엔티티에 새로운 필드를 추가했다.
```java
private LocalDateTime updatedAt; // 엔티티 내 새롭게 추가한 필드
```
JPA(Hibernate)가 "DB 테이블에도 updated_at 컬럼 존해할거다"라고 기대했는데,
이미 만들어져 있던 테이블에는 이 컬럼이 존재하지 않았다.

그래서 POST(INSERT) 할 때 JPA가 아래와 같은 SQL을 보내려가다 실패했다.
* insert into posts (..., updated_at, ...) values (...)
  * DB 테이블에 update_at 컬럼이 없으니 위 쿼리는 실패함.
  
결과적으로 500 Internal Server Error이 나온다.

#### 왜 ddl-auto:update인데도 이런 문제가 생길까?
ddl-auto: update는 "가능한 범위에서 스키마를 맞춰보겠다" 정도라서,
* 상황에 따라 컬럼 추가가 안 되거나,
* 이미 만들어진 테이블/인덱스/제약조건과 충돌하면 제대로 반영 안 될 떄가 있다.
* 특히 파일 DB로 계속 누적해서 쓰면, 스키마 변경이 반복되면서 꼬일 가능성이 커진다.

#### 해결방법
##### 방법1
* H2 콘솔에서 DROP TABLE POSTS;
* 그리고 서버 재시작하면, Hibernate가 다시 테이블을 만들면서 컬럼이 맞춰진다.
* 물론 이 방법은 기존 데이터가 다 날라간다.

##### 방법2
* 서버를 끈 상태에서 프로젝트 루트 기준으로
* ./data/crudboard* 파일을 삭제하면 완전히 새 DB가 됨.
* 다시 실행하면 스키마도 새로 생성됨.
* 물론 이 방법도 기존 데이터가 다 날라간다.

##### 실무적인 해결
* 실무에서는 "테이블 드랍/파일 삭제"를 못 하니까
* DB 마이그레이션 도구(Flyway/Liquibase)로 스키마 변경 SQL 스크립트로 관리한다.


### keyword 기능 구현 중 IgnoreCase 충돌 이슈
#### 기존 상황
```java
public class Post {
    ...
    @Lob
    @Column(nullable = false)
    private String content;
    ...
}
```
#### 문제 분석
* @Lob 애노테이션으로 Post 엔티티 title 칼럼이 CLOB(대용량 텍스트)로 매핑되어 있는 상황이였음
* keyword검색 기능 구현 중 Hibernate가 IgnoreCase(대소문자 무시)를 위해 만든 UPPER 같은 JPQL이 CLOB에 대해 UPPER를 적용하지 못하는 오류 발생

#### 왜 IgnoreCase에서 UPPER가 나오지?
* Spring Data JPA는 IgnoreCase가 붙으면 내부적으로 대소문자 무시를 위해 대략 아래와 같은 형태로 바꿔줌
* UPPER(field) LIKE UPPER(:param)
* 그런데 field가 CLOB이면 DB/Hibernate가 그 조합을 싫어한다.

#### 해결방법
* content 필드를 CLOB가 아니라 VARCHAR/TEXT로 매핑되게 바꾸기
* content 필드에 @Lob 애노테이션을 제거, @Colum 애노테이션에 length 명시해주기
* 이렇게 하면 content가 CLOB가 아니라 VARCHAR 계열로 잡혀서 UPPER가 가능해짐
```java
public class Post {
    ...
    @Column(nullable = false, length = 2000)
    private String content;
    ...
}
```