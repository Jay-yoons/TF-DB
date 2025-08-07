package com.restaurant.reservation.repository;

import com.restaurant.reservation.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    
    @Query("SELECT f FROM Favorite f WHERE f.userId = :userId")
    List<Favorite> findByUserId(@Param("userId") String userId);
    
    @Query("SELECT f FROM Favorite f WHERE f.userId = :userId AND f.storeId = :storeId")
    Optional<Favorite> findByUserIdAndStoreId(@Param("userId") String userId, @Param("storeId") String storeId);
    
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Favorite f WHERE f.userId = :userId AND f.storeId = :storeId")
    boolean existsByUserIdAndStoreId(@Param("userId") String userId, @Param("storeId") String storeId);
    
    @Query("SELECT f.storeId FROM Favorite f WHERE f.userId = :userId")
    List<String> findStoreIdsByUserId(@Param("userId") String userId);
    
    void deleteByUserIdAndStoreId(String userId, String storeId);
}
