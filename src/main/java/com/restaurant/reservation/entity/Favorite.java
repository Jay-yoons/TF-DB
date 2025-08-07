package com.restaurant.reservation.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "fav_store", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "store_id"})
})
@EntityListeners(AuditingEntityListener.class)
public class Favorite {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fav_store_id")
    private Long favStoreId;
    
    @Column(name = "user_id", nullable = false, length = 15)
    private String userId;
    
    @Column(name = "store_id", nullable = false, length = 20)
    private String storeId;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public Favorite() {}
    
    public Favorite(String userId, String storeId) {
        this.userId = userId;
        this.storeId = storeId;
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
