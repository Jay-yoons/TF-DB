package com.restaurant.reservation.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 사용자 즐겨찾기 가게 엔티티 클래스
 * 
 * 이 클래스는 사용자가 즐겨찾기한 가게 정보를 데이터베이스에 저장하기 위한 JPA 엔티티입니다.
 * FOG 팀의 데이터 사전에 따라 설계되었습니다.
 * 
 * 주요 필드:
 * - favStoreId: 즐겨찾기 고유 ID (기본키)
 * - userId: 사용자 ID (외래키)
 * - storeId: 가게 ID (외래키)
 * - createdAt: 생성일시 (자동 생성)
 * 
 * 제약조건:
 * - (userId, storeId) 복합 유니크 제약조건
 * 
 * @author FOG Team
 * @version 1.0
 * @since 2024-01-15
 */
@Entity
@Table(name = "FAV_STORE", uniqueConstraints = {
    @UniqueConstraint(name = "fav_store_un", columnNames = {"USER_ID", "STORE_ID2"})
})
@EntityListeners(AuditingEntityListener.class)
public class FavoriteStore {
    
    /**
     * 즐겨찾기 고유 ID (기본키)
     * 자동 증가
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fav_store_id")
    private Long favStoreId;
    
    /**
     * 사용자 ID (외래키)
     * 최대 15자까지 저장 가능
     */
    @Column(name = "USER_ID", nullable = false, length = 15)
    private String userId;
    
    /**
     * 가게 ID (외래키)
     * 최대 20자까지 저장 가능
     */
    @Column(name = "STORE_ID2", nullable = false, length = 20)
    private String storeId;
    
    /**
     * 생성일시
     * 자동 생성되며 수정 불가
     */
    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public FavoriteStore() {}
    
    public FavoriteStore(String userId, String storeId) {
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
