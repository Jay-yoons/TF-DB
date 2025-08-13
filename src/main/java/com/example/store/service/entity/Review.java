package com.example.store.service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "REVIEW",
    indexes = {
        @Index(name = "idx_review_store", columnList = "STORE_ID"),
        @Index(name = "idx_review_user", columnList = "USER_ID")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_review_store_user", columnNames = {"STORE_ID", "USER_ID"})
    }
)
/**
 * REVIEW 테이블 매핑 엔티티.
 * - 유니크 제약: (STORE_ID, USER_ID)
 * - 인덱스: STORE_ID, USER_ID
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @Column(name = "STORE_ID", length = 20, nullable = false)
    private String storeId;

    @Column(name = "USER_ID", length = 15, nullable = false)
    private String userId;

    @Column(name = "COMENT", length = 50)
    private String comment;

    @Column(name = "SCORE")
    private Integer score; // 1~5 범위는 서비스 레이어에서 검증 권장
}
