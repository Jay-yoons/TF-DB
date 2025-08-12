package com.restaurant.reservation.dto;

import java.time.LocalDateTime;

/**
 * 즐겨찾기 가게 정보를 담는 DTO 클래스
 */
public class FavoriteStoreDto {
    private Long favStoreId;
    private String storeId;
    private String storeName;
    private String userId;
    private LocalDateTime createdAt;

    // 기본 생성자
    public FavoriteStoreDto() {}

    // 전체 생성자
    public FavoriteStoreDto(Long favStoreId, String storeId, String storeName, String userId, LocalDateTime createdAt) {
        this.favStoreId = favStoreId;
        this.storeId = storeId;
        this.storeName = storeName;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    // Getter와 Setter
    public Long getFavStoreId() {
        return favStoreId;
    }

    public void setFavStoreId(Long favStoreId) {
        this.favStoreId = favStoreId;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
