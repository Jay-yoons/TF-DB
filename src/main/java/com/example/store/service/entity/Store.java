package com.example.store.service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

/**
 * STORES 테이블 매핑 엔티티.
 * - PK: STORE_ID (String)
 * - 카테고리 코드: Integer
 * - 영업시간: OPEN_TIME/CLOSE_TIME 우선 사용, 값이 없을 경우 SERVICE_TIME 문자열을 폴백으로 사용한다.
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

    // 신규: 영업 시간 컬럼 (둘 다 있을 때 우선 사용)
    @Column(name = "OPEN_TIME")
    private LocalTime openTime;

    @Column(name = "CLOSE_TIME")
    private LocalTime closeTime;

    @Column(name = "SERVICE_TIME", length = 50)
    @Deprecated
    private String serviceTime; // 기존 문자열 포맷 (폴백용)
}
