package com.restaurant.reservation.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "store_seat")
@EntityListeners(AuditingEntityListener.class)
public class StoreSeat {
    
    @Id
    @Column(name = "store_id")
    private String storeId;
    
    @Column(name = "in_using_seat", nullable = false)
    private Integer inUsingSeat = 0;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public StoreSeat() {}
    
    public StoreSeat(String storeId, Integer inUsingSeat) {
        this.storeId = storeId;
        this.inUsingSeat = inUsingSeat;
    }
    
    // Getters and Setters
    public String getStoreId() {
        return storeId;
    }
    
    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }
    
    public Integer getInUsingSeat() {
        return inUsingSeat;
    }
    
    public void setInUsingSeat(Integer inUsingSeat) {
        this.inUsingSeat = inUsingSeat;
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