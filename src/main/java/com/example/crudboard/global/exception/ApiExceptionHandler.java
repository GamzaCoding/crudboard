package com.example.crudboard.global.exception;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePostNotFound(PostNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("POST_NOT_FOUND", e.getMessage()));
    }
    public record ErrorResponse(
            String code,
            String message,
            Object details,
            LocalDateTime timestamp
    ){
        public static ErrorResponse of(String code, String message) {
            return new ErrorResponse(code, message, null, LocalDateTime.now());
        }
    }
}
/*
@RestControllerAdvice
@ControllerAdvice + @ResponseBody라고 생각하면된다.
@ResponseBody의 역할은 응답을 JSON으로 처리한다는 뜻이다.

@ControllerAdvice란 스프링 부트 에서 전역적으로 예외를 핸들링 할 수 있게 해주는 어노테이션이다.
이를 통해서 코드의 중복을 해결할 수 있다 -> 여러곳에 예외처리 필요 없음
한 클래스내에 정상 동작시 호출되는 코드와 예외를 처리하는 코드를 분리할 수 있다.
@ControllerAdvice는 @Component 어노테이션이 붙어 있기 때문에 Bean으로 등록된다.

주의 : 여러 ControllerAdvice가 있을 때 @Order 어노테이션으로 순서을 정하지 않으면 Spring은
ControllerAdvice를 임의의 순서로 호출한다. 즉 사용자가 예상하지 못한 예외처리가 발생할 수 있다.

Base Packages 속성을 이용해서 여러 ControllerAdvice를 세분화 할 수 있다.
작성된 패키지와 하위 패키지에서 발생하는 예외는 해당 ControllerAdvice에서 처리하도록 지정할 수 있다.
 */

/*
@ExceptionHandler(PostNotFoundException.class)
이 어노테이션이 붙어있는 메서드는 PostNotFoundException이 발생했을 때만 실행된다.
컨트롤러, 서비스 어디서든 PostNotFoundException이 밖으로 던져져 나오면
스프링이 이 핸들러 메서드를 찾아 호출한다.
 */

/*
ApiExceptionHandler를 만든 이유
기본 설정 그대로면 스프링이 알아서 에러를 내려준긴 하는데
응답 JSON 형식이 스프링 기본 포맷으로 들쭉날쭉하고
내가 원하는 코드/메시지 형태로 통일하기 어렵고
클라이언트(프론트/모바일)가 에러처리하기 불편해진다.
그래서 ApiExceptionHandler가 모든 컨트롤러에서 발생하는 예외를 한곳에서 처리한다.
404면 404답게, 내가 정의한 JSON 포멧으로 내려주도록 하기 위해서 만든것이다.
 */
