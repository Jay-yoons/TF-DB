package com.restaurant.reservation.repository;

import com.restaurant.reservation.entity.FavoriteStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 즐겨찾기 가게 Repository
 * 
 * 사용자의 즐겨찾기 가게 정보를 데이터베이스에서 조회, 저장, 수정, 삭제하는 기능을 제공합니다.
 * 
 * @author FOG Team
 * @version 1.0
 * @since 2024-01-15
 */
@Repository
public interface FavoriteStoreRepository extends JpaRepository<FavoriteStore, Long> {
    
    /**
     * 특정 사용자의 모든 즐겨찾기 가게 목록 조회
     * 
     * @param userId 사용자 ID
     * @return 해당 사용자의 즐겨찾기 가게 목록
     */
    List<FavoriteStore> findByUserId(String userId);
    
    /**
     * 특정 사용자가 특정 가게를 즐겨찾기했는지 확인
     * 
     * @param userId 사용자 ID
     * @param storeId 가게 ID
     * @return 즐겨찾기 정보 (존재하지 않으면 null)
     */
    Optional<FavoriteStore> findByUserIdAndStoreId(String userId, String storeId);
    
    /**
     * 특정 사용자의 즐겨찾기 가게 개수 조회
     * 
     * @param userId 사용자 ID
     * @return 즐겨찾기 가게 개수
     */
    long countByUserId(String userId);
    
    /**
     * 특정 사용자의 특정 가게 즐겨찾기 삭제
     * 
     * @param userId 사용자 ID
     * @param storeId 가게 ID
     */
    void deleteByUserIdAndStoreId(String userId, String storeId);
    
    /**
     * 특정 사용자의 모든 즐겨찾기 삭제
     * 
     * @param userId 사용자 ID
     */
    void deleteByUserId(String userId);
    
    /**
     * 특정 가게의 모든 즐겨찾기 삭제 (가게가 삭제될 때 사용)
     * 
     * @param storeId 가게 ID
     */
    void deleteByStoreId(String storeId);
    
    /**
     * 사용자 ID로 즐겨찾기한 가게 ID 목록만 조회
     * 
     * @param userId 사용자 ID
     * @return 즐겨찾기한 가게 ID 목록
     */
    @Query("SELECT fs.storeId FROM FavoriteStore fs WHERE fs.userId = :userId")
    List<String> findStoreIdsByUserId(@Param("userId") String userId);
    
    // =============================================================================
    // 뷰를 사용한 조회 메서드들 (DB 담당자와 협의 후 추가)
    // =============================================================================
    
    /**
     * 뷰를 사용하여 사용자의 즐겨찾기 가게 상세 정보 조회
     * V_USER_FAVORITE_STORES 뷰 사용
     * 
     * @param userId 사용자 ID
     * @return 즐겨찾기 가게 상세 정보 목록
     */
    @Query(value = "SELECT * FROM V_USER_FAVORITE_STORES WHERE USER_ID = :userId ORDER BY FAVORITE_CREATED_AT DESC", nativeQuery = true)
    List<Object[]> findFavoriteStoresWithDetails(@Param("userId") String userId);
    
    /**
     * 뷰를 사용하여 사용자의 리뷰 상세 정보 조회
     * V_USER_REVIEWS 뷰 사용
     * 
     * @param userId 사용자 ID
     * @return 사용자 리뷰 상세 정보 목록
     */
    @Query(value = "SELECT * FROM V_USER_REVIEWS WHERE USER_ID = :userId ORDER BY REVIEW_ID DESC", nativeQuery = true)
    List<Object[]> findUserReviewsWithDetails(@Param("userId") String userId);
    
    /**
     * 뷰를 사용하여 사용자의 예약 현황 조회
     * V_USER_BOOKINGS 뷰 사용
     * 
     * @param userId 사용자 ID
     * @return 사용자 예약 현황 목록
     */
    @Query(value = "SELECT * FROM V_USER_BOOKINGS WHERE USER_ID = :userId ORDER BY BOOKING_DATE DESC", nativeQuery = true)
    List<Object[]> findUserBookingsWithDetails(@Param("userId") String userId);
    
    /**
     * 뷰를 사용하여 사용자 대시보드 통계 조회
     * V_USER_DASHBOARD 뷰 사용
     * 
     * @param userId 사용자 ID
     * @return 사용자 대시보드 통계 정보
     */
    @Query(value = "SELECT * FROM V_USER_DASHBOARD WHERE USER_ID = :userId", nativeQuery = true)
    Object[] findUserDashboardStats(@Param("userId") String userId);
}
