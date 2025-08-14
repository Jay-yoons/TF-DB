package com.example.store.service.repository;

import com.example.store.service.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 리뷰 엔티티용 JPA 레포지토리.
 *
 * 설계 메모
 * - 엔티티: Review (PK: Long reviewId)
 * - 유니크 제약(서비스/DDL): 동일 사용자(USER_ID)가 동일 매장(STORE_ID)에 하나의 리뷰만 작성 가능
 * - 인덱스 권장: STORE_ID, USER_ID 각각 인덱싱하여 목록/내 리뷰 조회 최적화
 *
 * 트랜잭션/성능 가이드
 * - 목록은 페이징(Pageable)·정렬(Sort)을 붙여 확장하는 것을 권장 (예: 최신순)
 * - 평균 평점 계산은 서비스/쿼리에서 집계 함수로 처리하거나 캐시/물리화 고려
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * 특정 매장의 리뷰 전체 조회.
     * - 화면에서는 Pageable/Sort 적용을 권장(최신 작성순 등)
     *
     * @param storeId 매장 식별자
     * @return 리뷰 목록
     */
    List<Review> findByStoreId(String storeId);

    /**
     * 동일 사용자-매장 조합의 단일 리뷰 조회.
     * - 리뷰 중복 작성 방지 체크에 사용
     *
     * @param storeId 매장 식별자
     * @param userId  사용자 식별자
     * @return 존재 시 Optional<Review>, 없으면 Optional.empty()
     */
    Optional<Review> findByStoreIdAndUserId(String storeId, String userId);

    /**
     * 특정 사용자가 작성한 리뷰 전체 조회.
     * - 마이페이지/내 활동 보기 등에 사용
     *
     * @param userId 사용자 식별자
     * @return 리뷰 목록
     */
    List<Review> findByUserId(String userId);
}
