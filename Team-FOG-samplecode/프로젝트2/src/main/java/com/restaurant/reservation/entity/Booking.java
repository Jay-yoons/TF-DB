package com.restaurant.reservation.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking")
@EntityListeners(AuditingEntityListener.class)
public class Booking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "booking_booking_num_seq")
    @SequenceGenerator(name = "booking_booking_num_seq", sequenceName = "booking_booking_num_seq", allocationSize = 1)
    @Column(name = "booking_num")
    private Long bookingNum;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "store_id", nullable = false)
    private String storeId;
    
    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;
    
    @Column(name = "count", nullable = false)
    private Integer count;
    
    @Column(name = "booking_state_code", nullable = false)
    private String bookingStateCode;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Booking() {}
    
    public Booking(String userId, String storeId, LocalDate bookingDate, Integer count, String bookingStateCode) {
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