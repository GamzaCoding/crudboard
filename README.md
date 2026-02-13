# crudboard

Spring Boot 기반 **CRUD 게시판 API + 간단한 웹 UI(Thymeleaf 템플릿 + Vanilla JS Fetch)** 연습 프로젝트입니다.

- **게시글(Post)**: 생성/조회/수정/삭제(CRUD), 목록 조회(페이징/정렬), 검색(키워드/타입/기간)
- **댓글(Comment)**: 게시글 하위 리소스로 생성/목록/삭제(페이징)
- **인증(Auth)**: 회원가입/로그인/로그아웃/내 정보(`/me`) — **세션(HttpSession) 기반**
- **인가(Authorization)**:
    - 비로그인(Guest)도 조회/검색은 가능
    - **글/댓글 작성·수정·삭제는 로그인 필요**
- **에러 응답 표준화**: `ApiException + ErrorCode + GlobalExceptionHandler`로 응답 포맷 통일

> 본 프로젝트는 개인적인 학습, 연습 목적으로 제작되었습니다.

---

## Tech Stack

- Java 21
- Spring Boot 4.0.2
- Spring Web MVC
- Spring Data JPA (Hibernate)
- Spring Security
    - 세션 기반 로그인 (`HttpSessionSecurityContextRepository`)
    - dev 프로필에서 H2 Console은 HTTP Basic(ADMIN 계정)으로 보호
- Flyway (DB 마이그레이션)
- H2 Database
    - `dev`: file 모드
    - `test`: in-memory
- PostgreSQL (기본 실행/도커)
- Thymeleaf (템플릿 뷰)
- Front: Vanilla JS (ES Module) + Fetch
- springdoc-openapi (Swagger UI)
- Test: JUnit5, MockMvc
- Docker / docker-compose

---

## Quick Start

### 1) 로컬 실행 (dev 프로필 + H2)

`dev` 프로필에서는 H2(file) + H2 Console을 사용합니다.

> dev 프로필은 H2 Console을 **ADMIN Basic Auth**로 보호합니다.
> 아래 환경변수(`ADMIN_USERNAME`, `ADMIN_PASSWORD`)가 없으면 애플리케이션이 기동되지 않습니다.

```bash
export SPRING_PROFILES_ACTIVE=dev
export ADMIN_USERNAME=admin
export ADMIN_PASSWORD=admin1234

./gradlew bootRun
```

- App: http://localhost:8080
- H2 Console: http://localhost:8080/h2-console
    - (브라우저 Basic Auth 팝업이 뜨면 위 ADMIN 계정으로 로그인)
    - JDBC URL: `jdbc:h2:file:./data/crudboard;MODE=MySQL`
    - username: `sa`
    - password: (empty)

> dev 프로필 Flyway 위치: `classpath:db/migration`

---

### 2) 도커 실행 (PostgreSQL + App)

```bash
cp .env.example .env

docker compose up --build
```

- App: http://localhost:8080
- PostgreSQL: localhost:5432 (호스트에서 직접 접근 시)

> 기본 실행(프로필 미지정)에서는 Flyway 위치가 `classpath:db/migration-postgres`로 설정되어 있습니다.

---

### 3) 로컬에서 PostgreSQL로 실행 (도커 없이)

PostgreSQL을 별도로 실행해두고, 아래 환경변수를 맞춰 실행합니다.

```bash
export DB_URL='jdbc:postgresql://localhost:5432/crudboard'
export DB_USERNAME='crudboard'
export DB_PASSWORD='crudboard'

./gradlew bootRun
```

---

## Web UI

간단한 화면이 포함되어 있습니다.

- 템플릿: `src/main/resources/templates/**`
- 정적 리소스: `src/main/resources/static/**`
    - JS는 `static/js/**` 아래 ES Module로 분리되어 있습니다.

페이지:
- `/` : 홈(로그인/회원가입/게스트 진입)
- `/posts` : 게시글 목록(검색/정렬/페이징)
- `/posts/new` : 글쓰기(로그인 필요)
- `/posts/{id}` : 게시글 상세 + 댓글
- `/posts/{id}/edit` : 수정(로그인 필요)

---

## Swagger UI

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

---

## 인증/인가(세션) 동작 방식

- `/api/auth/login` 성공 시, 서버가 세션을 만들고 `SPRING_SECURITY_CONTEXT`를 세션에 저장합니다.
- 이후 요청에서는 브라우저/클라이언트가 세션 쿠키를 자동 포함하여 인증 상태가 유지됩니다.
- `Authentication.principal`에는 **userId(Long)** 를 넣도록 구현되어 있습니다.

### curl 예시 (쿠키 유지)

```bash
# 1) 회원가입
curl -i -X POST http://localhost:8080/api/auth/signup \
  -H 'Content-Type: application/json' \
  -d '{"email":"user1@example.com","password":"password123!"}'

# 2) 로그인 (쿠키 저장)
curl -i -c cookies.txt -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"user1@example.com","password":"password123!"}'

# 3) 내 정보 확인 (쿠키 사용)
curl -i -b cookies.txt http://localhost:8080/api/auth/me

# 4) 글쓰기 (쿠키 사용)
curl -i -b cookies.txt -X POST http://localhost:8080/api/posts \
  -H 'Content-Type: application/json' \
  -d '{"title":"hello","content":"first post"}'
```

---

## API Spec

Base URL: `http://localhost:8080`

### Auth

- `POST /api/auth/signup` → `201 Created`
- `POST /api/auth/login` → `204 No Content`
- `POST /api/auth/logout` → `204 No Content`
- `GET /api/auth/me` → 로그인 O: `200`, 로그인 X: `401`

---

### Posts

권한 요약
- **조회/목록/검색(GET)**: 누구나 가능
- **작성/수정/삭제(POST/PUT/DELETE)**: 로그인 필요

#### Create Post
- `POST /api/posts`
- Response
    - `201 Created`
    - `Location: /api/posts/{id}`

#### Get Post
- `GET /api/posts/{id}`

#### List Posts (Paging/Sort + 검색)
- `GET /api/posts?page=0&size=5&sort=createdAt,desc`

검색 파라미터(옵션)
- `keyword`: 검색어
- `type`: `TITLE | CONTENT | TITLE_CONTENT` (기본: `TITLE_CONTENT`)
- `createdFrom`, `createdTo`: 작성일 범위 (ISO_DATE_TIME)
    - 예: `2026-02-01T00:00:00`

> `size`는 최대 50으로 제한됩니다. (예: `size=1000` 요청 시 `50`으로 clamp)

#### Update Post
- `PUT /api/posts/{id}` → `204 No Content`

#### Delete Post
- `DELETE /api/posts/{id}` → `204 No Content`

---

### Comments (Post 하위 리소스)

Base: `/api/posts/{postId}/comments`

권한 요약
- **목록(GET)**: 누구나 가능
- **생성/삭제(POST/DELETE)**: 로그인 필요

- `GET /api/posts/{postId}/comments?page=0&size=10&sort=createdAt,desc`
- `POST /api/posts/{postId}/comments` → `201 Created`
- `DELETE /api/posts/{postId}/comments/{commentId}` → `204 No Content`

> 참고: `CommentCommandService`에는 update 메서드가 있지만, 현재 Controller에는 update endpoint가 연결되어 있지 않습니다. (TODO)

---

## Error Response

전역 예외 처리(`GlobalExceptionHandler`)로 예외 응답을 아래 포맷으로 통일했습니다.

```json
{
  "code": "POST_NOT_FOUND",
  "message": "게시글을 찾을 수 없습니다.",
  "fieldViolations": [],
  "path": "/api/posts/999999",
  "timestamp": "2026-02-13T12:00:00Z"
}
```

### Validation (400)
`@Valid` 실패 시 `code=VALIDATION_ERROR`이며, `fieldViolations`에 필드 에러가 담깁니다.

```json
{
  "code": "VALIDATION_ERROR",
  "message": "요청 값이 올바르지 않습니다.",
  "fieldViolations": [
    { "field": "title", "message": "must not be blank" }
  ],
  "path": "/api/posts",
  "timestamp": "2026-02-13T12:00:00Z"
}
```

---

## Tests

```bash
./gradlew test
```

- MockMvc 기반 통합 테스트로 아래 시나리오를 검증합니다.
    - Auth: 회원가입/로그인 후 `/me` 확인, 미로그인 시 401
    - Post: 작성/조회/수정/삭제, Not Found(404), Validation(400), 검색/페이징, size 제한(최대 50)
    - Comment: 생성/목록(게시글별 필터), 삭제 후 404, Validation(400)

---

## Project Structure (요약)

- `auth`
    - `AuthController`, `AuthService`
    - `dto` (`AuthRequest`, `MeResponse`)
- `global`
  - `error`(`ApiError`, `ApiException`, `ErrorCode`, `GlobalExceptionHandler`)
  - `dot` (`PageResponse`)
  - `security` (`SecurityConfig`)
- `user`
    - `User`, `UserRole`, `UserRepository`
- `post`
    - `Post`, `PostController`
    - `service` (`PostCommandService`, `PostQueryService`)
    - `repository` (`PostRepository`)
    - `dto` (`PostCreateRequest`, `PostUpdateRequest`, `PostResponse`)
    - `PostSearchCondition`, `PostSearchType`, `PostSpecifications`
- `comment`
    - `Comment`, `CommentController`
    - `service` (`CommentCommandService`, `CommentQueryService`)
    - `repository` (`CommentRepository`)
- `view`
    - `HomePageController`, `PostPageController`

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
