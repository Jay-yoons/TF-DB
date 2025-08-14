// Java
// [신규] 즐겨찾기 엔티티 - entity 패키지 “바로 아래”에 생성하세요.
package com.example.store.service.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 즐겨찾기 엔티티(FAV_STORE).
 * - [비즈니스] 동일 사용자-가게는 1개만 허용(Unique)
 * - [비정규화] 목록 성능/표시를 위해 storeName을 저장
 */
@Entity
@Table(
    name = "FAV_STORE",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_fav_store_user", columnNames = {"STORE_ID", "USER_ID"})
    },
    indexes = {
        @Index(name = "idx_fav_user", columnList = "USER_ID"),
        @Index(name = "idx_fav_store", columnList = "STORE_ID")
    }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavStore { // [중요] 파일명은 FavStore.java, public class도 FavStore로 동일

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long favStoreId; // PK(대체키)

    @Column(name = "STORE_ID", length = 20, nullable = false)
    private String storeId; // 가게 식별자

    // [비정규화] 매장명(목록 표시/성능 목적). 생성 시 Store에서 조회해 스냅샷 저장
    @Column(name = "STORE_NAME", length = 50)
    private String storeName;

    @Column(name = "USER_ID", length = 15, nullable = false)
    private String userId; // 사용자 식별자
}