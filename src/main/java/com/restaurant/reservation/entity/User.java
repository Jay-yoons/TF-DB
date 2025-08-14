package com.restaurant.reservation.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 사용자 엔티티 클래스
 * 
 * 이 클래스는 사용자 정보를 데이터베이스에 저장하기 위한 JPA 엔티티입니다.
 * FOG 팀의 데이터 사전에 따라 설계되었습니다.
 * 
 * 주요 필드:
 * - userId: 사용자 고유 ID (기본키)
 * - userName: 사용자 이름
 * - phoneNumber: 전화번호 (고유값)
 * - userLocation: 사용자 주소
 * - password: 암호화된 비밀번호
 * - isActive: 계정 활성화 상태
 * - createdAt: 생성일시 (자동 생성)
 * - updatedAt: 수정일시 (자동 업데이트)
 * 
 * @author FOG Team
 * @version 1.0
 * @since 2024-01-15
 */
@Entity
@Table(name = "USERS") // 데이터베이스 테이블명 지정
@EntityListeners(AuditingEntityListener.class) // 생성일시/수정일시 자동 관리
public class User {
    
    /**
     * 사용자 고유 ID (기본키)
     * 최대 15자까지 저장 가능
     */
    @Id
    @Column(name = "USER_ID", length = 50)
    private String userId;
    
    /**
     * 사용자 이름
     * 필수 입력 항목, 최대 20자
     */
    @Column(name = "USER_NAME", nullable = false, length = 20)
    private String userName;
    
    /**
     * 전화번호
     * 필수 입력 항목, 고유값, 최대 20자
     */
    @Column(name = "PHONE_NUMBER", nullable = false, unique = true, length = 20)
    private String phoneNumber;
    
    /**
     * 사용자 주소
     * 선택 입력 항목, 최대 50자
     */
    @Column(name = "USER_LOCATION", length = 50)
    private String userLocation;
    
    /**
     * 암호화된 비밀번호
     * 필수 입력 항목, 최대 255자 (암호화로 인한 길이 증가)
     */
    @Column(name = "PASSWORD", nullable = false, length = 255)
    private String password;
    
    /**
     * 계정 활성화 상태
     * 기본값: true (활성화)
     */
    @Column(name = "IS_ACTIVE", nullable = false)
    private boolean isActive = true;
    
    /**
     * 계정 생성일시
     * 자동 생성되며 수정 불가
     */
    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 계정 수정일시
     * 자동 업데이트
     */
    @LastModifiedDate
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
    
    // Constructors
    public User() {}
    
    public User(String userId, String userName, String phoneNumber, String userLocation, String password) {
        this.userId = userId;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.userLocation = userLocation;
        this.password = password;
    }
    
    // Getters and Setters
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
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getUserLocation() {
        return userLocation;
    }
    
    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
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