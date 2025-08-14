package com.restaurant.reservation.dto;

import java.time.LocalDateTime;

/**
 * 리뷰 정보를 담는 DTO 클래스
 */
public class ReviewDto {
    private Long reviewId;
    private String storeId;
    private String storeName;
    private String userId;
    private String userName;
    private String content;
    private Integer rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 기본 생성자
    public ReviewDto() {}

    // 전체 생성자
    public ReviewDto(Long reviewId, String storeId, String storeName, String userId, String userName, 
                    String content, Integer rating, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.reviewId = reviewId;
        this.storeId = storeId;
        this.storeName = storeName;
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.rating = rating;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getter와 Setter
    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
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
