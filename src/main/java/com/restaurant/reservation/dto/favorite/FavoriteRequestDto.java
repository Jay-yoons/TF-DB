package com.restaurant.reservation.dto.favorite;

public class FavoriteRequestDto {
    private String userId;
    private String storeId;
    
    // Constructors
    public FavoriteRequestDto() {}
    
    public FavoriteRequestDto(String userId, String storeId) {
        this.userId = userId;
        this.storeId = storeId;
    }
    
    // Getters and Setters
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
}
