package com.example.store.service.exception;

/**
 * 권한 부족(작성자 불일치 등) 상황을 표현하는 예외.
 * GlobalExceptionHandler에서 403(FORBIDDEN)으로 매핑된다.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
