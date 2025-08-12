package com.example.store.service.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * STORE_SEAT 테이블 매핑 엔티티.
 * - PK/FK: STORE_ID (STORES 참조)
 * - IN_USING_SEAT: 현재 사용 중 좌석 수
 * - @Version: 낙관적 락으로 동시 갱신 충돌 감지
 */
@Entity
@Table(name = "STORE_SEAT")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreSeat {
    @Id
    @Column(name = "STORE_ID", length = 20, nullable = false)
    private String storeId; // STORES.STORE_ID 참조 (VARCHAR2(20))
    
    @Column(name = "IN_USING_SEAT")
    private int inUsingSeat;

    @Version
    private Long version;
}
