package com.restaurant.reservation.dto;

import java.time.LocalDateTime;

public class StoreDto {
    private String storeId;
    private String storeName;
    private String storeLocation;
    private Integer seatNum;
    private String categoryCode;
    private String serviceTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public StoreDto() {}
    
    public StoreDto(String storeId, String storeName, String storeLocation, Integer seatNum, String categoryCode, String serviceTime) {
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