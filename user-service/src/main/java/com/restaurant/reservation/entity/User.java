package com.restaurant.reservation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사용자 엔티티 클래스
 * 주요 필드:
 * - userId: 사용자 고유 ID (기본키)
 * - userName: 사용자 이름
 * - phoneNumber: 전화번호 (고유값)
 * - userLocation: 사용자 주소
 */
@Entity
@Table(name = "USERS") // 데이터베이스 테이블명 지정
@NoArgsConstructor
@Getter
@Setter
public class User {
    
    /**
     * 사용자 고유 ID (기본키)
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

    public User(String userId, String userName, String phoneNumber, String userLocation) {
        this.userId = userId;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.userLocation = userLocation;
    }
} 