package com.example.crudboard.global.error;

import com.example.crudboard.global.error.ApiError.FieldViolation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 비즈니스로직 에러 예외
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(ApiException e, HttpServletRequest req) {
        log.info("Unhandled exception path={}", req.getRequestURI(), e);

        ErrorCode code = e.getErrorCode();

        return ResponseEntity
                .status(code.status())
                .body(new ApiError(
                        code.name(),
                        e.getMessage(),
                        List.of(),
                        req.getRequestURI(),
                        Instant.now()
                ));
    }

    // @Valid 바인딩 실패 (DTO validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgNotValid(MethodArgumentNotValidException e, HttpServletRequest req) {
        log.info("Unhandled exception path={}", req.getRequestURI(), e);

        List<ApiError.FieldViolation> violations = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toViolation)
                .toList();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(
                        ErrorCode.VALIDATION_ERROR.name(),
                        ErrorCode.VALIDATION_ERROR.defaultMessage(),
                        violations,
                        req.getRequestURI(),
                        Instant.now()
                ));
    }

    // @Validated + request param/path validation 실패 등
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException e, HttpServletRequest req) {
        log.info("Unhandled exception path={}", req.getRequestURI(), e);

        List<ApiError.FieldViolation> violations = e.getConstraintViolations()
                .stream()
                .map(v -> new ApiError.FieldViolation(
                        v.getPropertyPath() != null ? v.getPropertyPath().toString() : "",
                        v.getMessage()
                ))
                .collect(Collectors.toList());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(
                        ErrorCode.VALIDATION_ERROR.name(),
                        ErrorCode.VALIDATION_ERROR.defaultMessage(),
                        violations,
                        req.getRequestURI(),
                        Instant.now()
                ));
    }

    // JSON 파싱 실패(잘못된 JSON)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleNotReadable(HttpMessageNotReadableException e, HttpServletRequest req) {
        log.info("Unhandled exception path={}", req.getRequestURI(), e);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(
                        ErrorCode.VALIDATION_ERROR.name(),
                        "요청 본문(JSON)이 올바르지 않습니다.",
                        List.of(),
                        req.getRequestURI(),
                        Instant.now()
                ));
    }

    // 로그인 실패
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException e, HttpServletRequest req) {
        log.info("Unhandled exception path={}", req.getRequestURI(), e);

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError(
                        ErrorCode.BAD_VALUE_OF_EMAIL_OR_PASSWORD.name(),
                        ErrorCode.BAD_VALUE_OF_EMAIL_OR_PASSWORD.defaultMessage(),
                        List.of(),
                        req.getRequestURI(),
                        Instant.now()
                ));
    }

    // 권한 없음
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException e, HttpServletRequest req) {
        log.info("Unhandled exception path={}", req.getRequestURI(), e);

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ApiError(
                        ErrorCode.FORBIDDEN.name(),
                        ErrorCode.FORBIDDEN.defaultMessage(),
                        List.of(),
                        req.getRequestURI(),
                        Instant.now()
                ));
    }

    // 그 외 모든 예외(진짜 서버 에러)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnknown(Exception e, HttpServletRequest req) {
        log.info("Unhandled exception path={}", req.getRequestURI(), e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(
                        ErrorCode.INTERNAL_ERROR.name(),
                        ErrorCode.INTERNAL_ERROR.defaultMessage(),
                        List.of(),
                        req.getRequestURI(),
                        Instant.now()
                ));
    }

    private FieldViolation toViolation(FieldError fe) {
        String field = fe.getField();
        String msg = fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid";
        return new FieldViolation(field, msg);
    }
}
