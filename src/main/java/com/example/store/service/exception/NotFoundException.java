package com.example.store.service.exception;

/**
 * 요청한 리소스를 찾을 수 없을 때 사용하는 예외.
 * GlobalExceptionHandler에서 404(NOT_FOUND)로 매핑된다.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
