package com.restaurant.reservation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BookingDto {
    private Long bookingNum;
    private String userId;
    private String storeId;
    private LocalDate bookingDate;
    private Integer count;
    private String bookingStateCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public BookingDto() {}
    
    public BookingDto(String userId, String storeId, LocalDate bookingDate, Integer count, String bookingStateCode) {
        this.userId = userId;
        this.storeId = storeId;
        this.bookingDate = bookingDate;
        this.count = count;
        this.bookingStateCode = bookingStateCode;
    }
    
    // Getters and Setters
    public Long getBookingNum() {
        return bookingNum;
    }
    
    public void setBookingNum(Long bookingNum) {
        this.bookingNum = bookingNum;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getStoreId() {
        return storeId;
    }
    
    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }
    
    public LocalDate getBookingDate() {
        return bookingDate;
    }
    
    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }
    
    public Integer getCount() {
        return count;
    }
    
    public void setCount(Integer count) {
        this.count = count;
    }
    
    public String getBookingStateCode() {
        return bookingStateCode;
    }
    
    public void setBookingStateCode(String bookingStateCode) {
        this.bookingStateCode = bookingStateCode;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
} 