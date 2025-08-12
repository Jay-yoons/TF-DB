package com.example.store.service.repository;

import com.example.store.service.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 리뷰 엔티티용 JPA 레포지토리.
 * - 가게별/사용자별 조회, 사용자/가게 중복 체크 제공
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByStoreId(String storeId);
    Optional<Review> findByStoreIdAndUserId(String storeId, String userId);
    List<Review> findByUserId(String userId);
}
