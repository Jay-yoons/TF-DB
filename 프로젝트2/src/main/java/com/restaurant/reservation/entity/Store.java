package com.restaurant.reservation.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "stores")
@EntityListeners(AuditingEntityListener.class)
public class Store {
    
    @Id
    @Column(name = "store_id")
    private String storeId;
    
    @Column(name = "store_name", nullable = false)
    private String storeName;
    
    @Column(name = "store_location", nullable = false)
    private String storeLocation;
    
    @Column(name = "seat_num", nullable = false)
    private Integer seatNum;
    
    @Column(name = "category_code")
    private String categoryCode;
    
    @Column(name = "service_time")
    private String serviceTime;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Store() {}
    
    public Store(String storeId, String storeName, String storeLocation, Integer seatNum, String categoryCode, String serviceTime) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.storeLocation = storeLocation;
        this.seatNum = seatNum;
        this.categoryCode = categoryCode;
        this.serviceTime = serviceTime;
    }
    
    // Getters and Setters
    public String getStoreId() {
        return storeId;
    }
    
    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }
    
    public String getStoreName() {
        return storeName;
    }
    
    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
    
    public String getStoreLocation() {
        return storeLocation;
    }
    
    public void setStoreLocation(String storeLocation) {
        this.storeLocation = storeLocation;
    }
    
    public Integer getSeatNum() {
        return seatNum;
    }
    
    public void setSeatNum(Integer seatNum) {
        this.seatNum = seatNum;
    }
    
    public String getCategoryCode() {
        return categoryCode;
    }
    
    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }
    
    public String getServiceTime() {
        return serviceTime;
    }
    
    public void setServiceTime(String serviceTime) {
        this.serviceTime = serviceTime;
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