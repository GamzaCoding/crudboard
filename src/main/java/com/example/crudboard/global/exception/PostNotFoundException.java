package com.example.crudboard.global.exception;

public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(Long id) {
        super("Post not found. id=" + id);
    }

    /*
    PostNotFoundException을 왜 만들었나?
    이유
    단순히 NoSuchElementException 같은 걸 던지면 "무슨 리소스가 없었는지" 의미가 약함
    PostNotFoundException은 이름만 봐도 의미가 명확하다.
    나중에 @RestControllerAdvice로 잡아서 404 NOT FOUND로 바꾸기 쉬움
    즉, "예외를 커스텀한다"는 건 에러 정책을 설계하기 위한 준비다.
     */
}
