package fog.review_service.repository;

import fog.review_service.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByUserId(String userId);
    Optional<Review> findByStoreIdAndUserId(String storeId, String userId);
    List<Review> findByStoreId(String storeId);
}