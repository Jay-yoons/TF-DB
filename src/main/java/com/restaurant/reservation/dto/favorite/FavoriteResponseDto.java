package com.restaurant.reservation.dto.favorite;

import java.time.LocalDateTime;

public class FavoriteResponseDto {
    private Long favStoreId;
    private String userId;
    private String storeId;
    private String storeName;
    private String storeLocation;
    private String categoryCode;
    private Double averageRating;
    private Boolean isOpen;
    private LocalDateTime createdAt;
    
    // Constructors
    public FavoriteResponseDto() {}
    
    public FavoriteResponseDto(Long favStoreId, String userId, String storeId, String storeName, 
                             String storeLocation, String categoryCode, Double averageRating, 
                             Boolean isOpen, LocalDateTime createdAt) {
        this.favStoreId = favStoreId;
        this.userId = userId;
        this.storeId = storeId;
        this.storeName = storeName;
        this.storeLocation = storeLocation;
        this.categoryCode = categoryCode;
        this.averageRating = averageRating;
        this.isOpen = isOpen;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public Long getFavStoreId() {
        return favStoreId;
    }
    
    public void setFavStoreId(Long favStoreId) {
        this.favStoreId = favStoreId;
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
    
    public String getCategoryCode() {
        return categoryCode;
    }
    
    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }
    
    public Double getAverageRating() {
        return averageRating;
    }
    
    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }
    
    public Boolean getIsOpen() {
        return isOpen;
    }
    
    public void setIsOpen(Boolean isOpen) {
        this.isOpen = isOpen;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
