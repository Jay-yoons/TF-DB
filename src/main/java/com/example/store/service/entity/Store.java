package com.example.store.service.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * STORES 테이블 매핑 엔티티.
 * - PK: STORE_ID (String)
 * - 카테고리 코드: Integer
 * - 영업시간은 스키마 정의에 따라 문자열로 저장한다.
 */
@Entity
@Table(name = "STORES")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store {
    @Id
    @Column(name = "STORE_ID", length = 20, nullable = false)
    private String storeId; // DB: VARCHAR2(20)

    @Column(name = "STORE_NAME", length = 50)
    private String storeName;
    
    @Column(name = "CATEGORY_CODE")
    private Integer categoryCode; // DB: INTEGER
    
    @Column(name = "STORE_LOCATION", length = 50)
    private String storeLocation; // 예: "서울 강남구"
    
    @Column(name = "SEAT_NUM")
    private int seatNum;
    
    @Column(name = "SERVICE_TIME", length = 50)
    private String serviceTime; // DB: VARCHAR2(50)

    @Column(name = "LONGITUDE", length = 50)
    private String longitude; // 경도

    @Column(name = "LATITUDE", length = 50)
    private String latitude; // 위도
}
