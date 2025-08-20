package com.example.store.service.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * STORE_LOCATION 테이블 매핑 엔티티.
 * - PK/FK: STORE_ID (STORES 참조)
 * - 위경도: LONGITUDE, LATITUDE
 */
@Entity
@Table(name = "STORE_LOCATION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreLocation {

    @Id
    @Column(name = "STORE_ID", length = 20, nullable = false)
    private String storeId;

    @Column(name = "LONGITUDE", length = 50)
    private String longitude;

    @Column(name = "LATITUDE", length = 50)
    private String latitude;
}
