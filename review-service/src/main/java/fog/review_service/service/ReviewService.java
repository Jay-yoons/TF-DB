package fog.review_service.service;

import fog.review_service.client.UserServiceResponseClient;
import fog.review_service.dto.ReviewDto;
import fog.review_service.dto.UserInfoDto;
import fog.review_service.dto.ReviewRequestDto;
import fog.review_service.entity.Review;
import fog.review_service.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserServiceResponseClient userServiceResponseClient;

    public List<ReviewDto> getMyReviews(String userId) {
        return reviewRepository.findByUserId(userId).stream()
                .map(ReviewDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 특정 가게(storeId)에 대한 리뷰 목록 반환
    public List<ReviewDto> getMyReviewsByStore(String userId, String storeId) {
        return reviewRepository.findByStoreId(storeId).stream()
                .filter(r -> r.getUserId().equals(userId))
                .map(ReviewDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 특정 가게의 모든 리뷰 목록 반환 (가게 페이지용)
    public List<ReviewDto> getStoreReviews(String storeId) {
        return reviewRepository.findByStoreId(storeId).stream()
                .map(ReviewDto::fromEntity)
                .collect(Collectors.toList());
    }

    public ReviewDto getReview(Long id) {
        return reviewRepository.findById(id)
                .map(ReviewDto::fromEntity)
                .orElseThrow(() -> new RuntimeException("리뷰가 없습니다."));
    }

    public ReviewDto createReview(String storeId, String userId, ReviewRequestDto dto) {
        Optional<Review> existing = reviewRepository.findByStoreIdAndUserId(storeId, userId);
        if (existing.isPresent()) throw new RuntimeException("이미 작성한 리뷰입니다.");

        Review review = Review.builder()
                .storeId(storeId)
                .userId(userId)
                .comment(dto.getComment())
                .score(dto.getScore())
                .build();

        return ReviewDto.fromEntity(reviewRepository.save(review));
    }

    public ReviewDto updateReview(Long id, ReviewRequestDto dto) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("리뷰 없음"));

        review.setComment(dto.getComment());
        review.setScore(dto.getScore());

        return ReviewDto.fromEntity(reviewRepository.save(review));
    }

    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }

    public List<ReviewDto> getReviewsWithUser() {
        return reviewRepository.findAll().stream()
                .map(review -> {
                    try {
                        UserInfoDto userInfo = userServiceResponseClient.getUserInfo(review.getUserId());
                        return ReviewDto.fromEntity(review, userInfo);
                    } catch (Exception e) {
                        // UserService 호출 실패 시 사용자 정보 없이 리뷰만 반환
                        return ReviewDto.fromEntity(review);
                    }
                })
                .collect(Collectors.toList());
    }
}
