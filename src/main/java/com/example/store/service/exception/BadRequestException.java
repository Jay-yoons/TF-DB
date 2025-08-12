package com.example.store.service.exception;

/**
 * 잘못된 요청(유효성 위반, 포맷 오류, 비즈니스 규칙 위반 등)을 표현하는 예외.
 * GlobalExceptionHandler에서 400(BAD_REQUEST)으로 매핑된다.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
