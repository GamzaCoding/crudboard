# crudboard

Spring Boot 기반 **CRUD 게시판 REST API** 연습 프로젝트입니다.  
게시글(Post)에 대해 **생성/조회/수정/삭제(CRUD)**, **목록 조회(페이징/정렬)**, **키워드 검색**, **예외 처리**, **통합 테스트(MockMvc)** 를 구현했습니다.
또한 게시글에 종속되는 댓글(Comment) CRUD(및 목록 페이징)를 추가했습니다.

> ✨ 서비스 레이어는 책임을 분리하기 위해  
> **PostCommandService(쓰기: Create/Update/Delete)**, **PostQueryService(읽기: Get/List/Search)** 로 구성했습니다.
> (Comment CRUD도 동일한 방식의 레이어를 사용했습니다.)

---

## Tech Stack
- Java 21
- Spring Boot 4.0.2
- Spring Web (Spring MVC)
- Spring Data JPA (Hibernate)
- H2 Database (local: file / test: in-memory)
- Flyway (DB 마이그레이션)
- JPA Auditing (createAt/updateAt 자동 관리)
- Validation (jakarta validation)
- JUnit5, MockMvc
---

## Getting Started

### 1) Run
```bash
./gradlew bootRun
```
- Server: http://localhost:8080

### 2) H2 Console
- URL: http://localhost:8080/h2-console
- JDBC URL 예시: `jdbc:h2:file:./data/crudboard;MODE=MySQL`
- username: `sa`
- password: (empty)

### 3) DB 마이그레이션(Flyway)
- 애플리케이션 시작 시 db/migration 아래의 SQL 마이그레이션을 자동 적용합니다.
- 파일명 규칙 예: V1__init.sql, V2__add_column.sql 처럼, 버전 + 설명 형태


> 주의: 이미 적용된 마이그레이션 파일(V2 등)의 내용을 나중에 수정하면 
> Flyway가 checksum mismatch로 실행을 막을 수 있습니다.
> (그럴 땐 파일 내용을 원복하거나, 새 버전(V3/V4...)으로 변경사항을 추가하는 방식이 안전합니다.


---

## API Spec
Base URL: `http://localhost:8080`

### Create Post
- `POST /api/posts`
- Request Body
```json
{
  "title": "hello",
  "content": "first post"
}
```
- Response
  - `201 Created`
  - `Location: /api/posts/{id}`

### Get Post
- `GET /api/posts/{id}`
- Response: `200 OK`
```json
{
  "id": 1,
  "title": "hello",
  "content": "first post",
  "createdAt": "2026-02-03T20:05:27.098914",
  "updatedAt": "2026-02-03T20:05:27.098931"
}
```

### List Posts (Paging/Sort)
- `GET /api/posts?page=0&size=5&sort=createdAt,desc`
- Response: `200 OK`
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

### Search Posts (고도화된 검색)
#### 1) 키워드만 검색 (기본: TITLE_CONTENT)
- `GET /api/posts?keyword=hello&page=0&size=10&sort=createdAt,desc`

#### 2) 검색 타입 지정 (TITLE / CONTENT / TITLE_CONTENT)
- `GET /api/posts?keyword=스프링&type=TITLE&page=0&size=10`
- `GET /api/posts?keyword=스프링&type=CONTENT&page=0&size=10`

#### 3) 작성일 범위(createdAt) 필터
- `GET /api/posts?createdFrom=2026-02-01T00:00:00&createdTo=2026-02-06T23:59:59&page=0&size=10`

#### 4) 조합 예시
- `GET /api/posts?keyword=스프링&type=TITLE&createdFrom=2026-02-01T00:00:00&createdTo=2026-02-06T23:59:59&page=0&size=10`

> 내부 구현은 **JPA Specification**(Criteria API 기반)을 이용해  
> keyword/type/기간 조건을 “있는 것만” 조합하는 방식으로 동적 쿼리를 생성합니다.

### Update Post
- `PUT /api/posts/{id}`
- Request Body
```json
{
  "title": "updated title",
  "content": "updated content"
}
```
- Response: `204 No Content`

### Delete Post
- `DELETE /api/posts/{id}`
- Response: `204 No Content`

---
## Comment API (Post 하위 리소스)

댓글은 “게시글에 종속”되므로 보통 아래 형태로 설계합니다.

### Create Comment
* POST /api/posts/{postId}/comments
```json
{
  "content": "첫 댓글"
}
```
* Response: 201 Created (+ Location 또는 body 반환 방식은 프로젝트 정책에 따름)

### List Comments (Paging)
* GET /api/posts/{postId}/comments?page=0&size=10&sort=createdAt,desc
* Response: 200 OK (Page 형태)

```json
{
  "content": [
    {
      "id": 1,
      "content": "첫 댓글",
      "createdAt": "2026-02-07T10:00:00.000000",
      "updatedAt": "2026-02-07T10:00:00.000000"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0,
  "first": true,
  "last": true
}
```

### Update Comment
* PUT /api/posts/{postId}/comments/{commentId}
```json
{
  "content": "수정된 댓글"
}
```
* Response: 204 No Content

### Delete Comment
* DELETE /api/posts/{postId}/comments/{commentId}
* Response: 204 No Content

---

## Error Response

### Post Not Found
- Response: `404 Not Found`
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

- MockMvc 기반 통합 테스트로 아래 시나리오를 검증합니다.
  - Create: `201 Created` + `Location` 헤더
  - Read: `200 OK` + 응답 JSON 검증
  - Not Found: `404` + 에러 코드 검증
  - List: 페이징/정렬 동작 확인
  - Search: keyword 검색 동작 확인
  - Update: `204` + 수정 반영 확인
  - Delete: `204` + 삭제 후 `404` 확인

---

## Project Structure
- `post`
  - `Post`
  - `PostController`
  - `service`
    - `PostCommandService`
    - `PostQueryService`
  - `repository`
    - `PostRepository`
  - `dto`
    - `PostCreateRequest`
    - `PostUpdateRequest`
    - `PostResponse`
- `comment`
  - `Comment`
  - `CommentController`
  - `service`
    - `CommentCommandService`
    - `CommentQuerySerive`
  - `repository`
    - `CommentRepositoy`
  - `dto`
    - `CommentCreateRequest`
    - `CommentResponse`
    - `CommentUpdateRequest`
- `global`
  - `dto`
    - `PageResponse`
  - `exception`
    - `ApiExceptionHandler`
    - `PostNotFoundException`
- `view`
  - `PostPageController`
- `JpaAuditingConfig`

---

## 트러블 슈팅

### H2 컬럼 불일치 이슈

#### 기존 상황
H2 DB를 file로 사용 중이므로 서버를 껐다가 켜도 이전 테이블 구조가 남아 있습니다.
```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/crudboard;MODE=MySQL
```

#### 문제 상황 발생
개발 도중 엔티티에 새로운 필드를 추가했습니다.
```java
private LocalDateTime updatedAt; // 엔티티 내 새롭게 추가한 필드
```

JPA(Hibernate)가 "DB 테이블에도 updated_at 컬럼이 존재할 것이다"라고 기대했는데,  
이미 만들어져 있던 테이블에는 이 컬럼이 존재하지 않아 `INSERT` 시점에 실패할 수 있습니다.

#### 왜 ddl-auto:update인데도 이런 문제가 생길까?
`ddl-auto: update`는 "가능한 범위에서 스키마를 맞춰보겠다" 수준이라서,
- 상황에 따라 컬럼 추가가 안 되거나
- 이미 만들어진 테이블/인덱스/제약조건과 충돌하면 제대로 반영이 안 될 때가 있습니다.
- 특히 파일 DB로 누적 사용하면, 스키마 변경이 반복되면서 꼬일 가능성이 커집니다.

#### 해결방법
- 방법1: H2 콘솔에서 `DROP TABLE POSTS;` 후 서버 재시작 (데이터 삭제)
- 방법2: 서버를 끈 상태에서 프로젝트 루트 기준 `./data/crudboard*` 파일 삭제 후 재실행 (데이터 삭제)
- 실무적인 해결: Flyway/Liquibase 등 마이그레이션 도구로 변경 스크립트를 관리

---

### Keyword 기능 구현 중 IgnoreCase 충돌 이슈

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
- `@Lob`로 `content`가 CLOB(대용량 텍스트)로 매핑된 상태에서
- keyword 검색(`IgnoreCase`)을 구현하면서 Hibernate가 내부적으로 `UPPER(field)` 같은 형태로 변환하는데,
- DB/Hibernate가 CLOB에 대한 `UPPER` 적용을 허용하지 않아 오류가 발생할 수 있습니다.

#### 해결방법
- `content`를 CLOB가 아닌 VARCHAR/TEXT 계열로 매핑되도록 변경
- `@Lob` 제거 후 `length` 등을 명시
```java
public class Post {
    ...
    @Column(nullable = false, length = 2000)
    private String content;
    ...
}
```
---
### Flyway 마이그레이션 파일 수정으로 인한 checksum mismatch 이슈

#### 발생 흐름
1. V5__create_comments.sql 작성 후 애플리케이션 실행
2. SQL 문법 오타로 인해 Flyway migration 실패
3. flyway_schema_history에 실패 기록이 남아 다음 실행에서 validation 살패
4. "오타만 고치면 되겠지"하고 이미 실패/적용된 V5 파일을 수정
5. Flyway가 checksum mismatch 또는 failed migration detected로 애플리케이션 부팅을 막음

#### 핵심 원인
* Flyway는 한 번 적용된 버전 파일의 내용이 바뀌면 안 된다는 전제를 강하게 가집니다.
* 실패한 버전의 기록이 남아있으면 validate 단계에서 실행을 중단합니다.

#### 해결 방식
* DB에 반쪽짜리로 생성된 흔적 정리
  * comments 테이블/인덱스/제약조건이 일부라도 만들어졌다면 제거
  * DB에 접속하여 Drop table 실행
* 그 다음, Flyway History 정합성 복구
  * DB에 접속하여 flyway_schema_history 테이블 내 실패 기록 제거