package com.restaurant.reservation.dto;

import java.time.LocalDateTime;

/**
 * 프론트엔드와의 일관된 API 응답을 위한 표준 응답 DTO
 * 
 * @author Team-FOG User Service
 * @version 1.0
 * @since 2024-01-15
 */
public class ApiResponseDto<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private String errorCode;

    // 기본 생성자
    public ApiResponseDto() {
        this.timestamp = LocalDateTime.now();
    }

    // 성공 응답 생성자
    public ApiResponseDto(T data) {
        this();
        this.success = true;
        this.data = data;
        this.message = "요청이 성공적으로 처리되었습니다.";
    }

    // 성공 응답 생성자 (메시지 포함)
    public ApiResponseDto(T data, String message) {
        this();
        this.success = true;
        this.data = data;
        this.message = message;
    }

    // 에러 응답 생성자
    public ApiResponseDto(String message, String errorCode) {
        this();
        this.success = false;
        this.message = message;
        this.errorCode = errorCode;
    }

    // Getter와 Setter
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    // 정적 팩토리 메서드
    public static <T> ApiResponseDto<T> success(T data) {
        return new ApiResponseDto<>(data);
    }

    public static <T> ApiResponseDto<T> success(T data, String message) {
        return new ApiResponseDto<>(data, message);
    }

    public static <T> ApiResponseDto<T> error(String message) {
        return new ApiResponseDto<>(message, "ERROR");
    }

    public static <T> ApiResponseDto<T> error(String message, String errorCode) {
        return new ApiResponseDto<>(message, errorCode);
    }
}
