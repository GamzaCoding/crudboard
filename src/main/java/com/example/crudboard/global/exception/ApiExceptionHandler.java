package com.example.crudboard.global.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePostNotFound(PostNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("POST_NOT_FOUND", e.getMessage()));
    }

    /*
    MethodArgumentNotValidException은 @RequestBody, @Valid가 실패하면 터지는 예외다.
    fieldErrors를 {필드명: 메시지} 형태로 만들어 details에 넣었다

    왜 LinkedHashMap인가?
    에러가 여러 개일 때 등록된 순서를 유지해서, 응답이 매번 같은 순서로 나와 테스트/디버깅이 편하다.
    400 코드: 클라이언트 입력갑이 잘못됨
     */

    /*
    예외 처리 흐름
    1. 요청 들어옴 -> 컨트롤러 호출 직전에 검증 실패 -> MethodArgumentNotValidException 발생
    2. DispatcherServlet이 예외를 잡고 "예외 처리자"를 찾음(@ExceptionHandler 애노테이션이 붙은 매서드를 전부 탐색한다.)
    3. handleValidation(MethodArgumentNotValidException e) 실행 -> ResponseEntity<ErrorResponse> 반환
    4. 이 클래스에 붙은 애노테이션 @RestControllerAdvice이 기능 발휘
    5. Spring MVC는 HttpMessageConvert를 통해 Jackson이 ErrorResponse를 JONS으로 직렬화 해줌
    6. 최종적으로 HTTP response body에 직렬화된 JSON 내용을 작성해서 클라이언트에게 전송해줌
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(err -> fieldErrors.put(err.getField(), err.getDefaultMessage()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("VALIDATION_ERROR", "Validation failed", fieldErrors));
    }


    @Schema(name = "ErrorResponse", description = "API 에러 응답 포맷")
    public record ErrorResponse(
            @Schema(example = "POST_NOT_FOUND")
            String code,
            @Schema(example = "Post not found, id=1")
            String message,
            Object details,
            @Schema(example = "2026-02-04T14:57:49.437688")
            LocalDateTime timestamp
    ){
        public static ErrorResponse of(String code, String message) {
            return new ErrorResponse(code, message, null, java.time.LocalDateTime.now());
        }

        public static ErrorResponse of(String code, String message, Object details) {
            return new ErrorResponse(code, message, details, LocalDateTime.now());
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
