package com.example.store.service.service;

import com.example.store.service.dto.ReviewDto;
import com.example.store.service.dto.ReviewRequestDto;
import com.example.store.service.entity.Review;
import com.example.store.service.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.store.service.exception.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Objects;

/**
 * 리뷰 비즈니스 로직을 담당하는 서비스.
 * - 평점 범위(1~5) 검증
 * - 사용자/가게 중복 리뷰 방지
 * - 수정/삭제 권한(작성자 본인) 검증
 */
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    /**
     * 특정 가게의 모든 리뷰 목록 반환.
     */
    public List<ReviewDto> getStoreReviews(String storeId) {
        return reviewRepository.findByStoreId(storeId).stream()
                .map(ReviewDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 작성자 본인의 모든 리뷰 목록 반환.
     */
    public List<ReviewDto> getMyReviews(String userId) {
        return reviewRepository.findByUserId(userId).stream()
                .map(ReviewDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 가게에서 작성자 본인이 작성한 리뷰 목록 반환.
     */
    public List<ReviewDto> getMyReviewsByStore(String userId, String storeId) {
        return reviewRepository.findByStoreId(storeId).stream()
                .filter(r -> r.getUserId().equals(userId))
                .map(ReviewDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 리뷰 단건 조회.
     */
    public ReviewDto getReview(Long id) {
        return reviewRepository.findById(id)
                .map(ReviewDto::fromEntity)
                .orElseThrow(() -> new NotFoundException("리뷰가 없습니다."));
    }

    /**
     * 리뷰 작성. 동일 사용자/가게 중복은 허용하지 않는다.
     */
    public ReviewDto createReview(ReviewRequestDto dto) {
        validateScore(dto.getScore());
        if (reviewRepository.findByStoreIdAndUserId(dto.getStoreId(), dto.getUserId()).isPresent()) {
            throw new BadRequestException("이미 작성한 리뷰입니다.");
        }
        Review review = Review.builder()
                .storeId(dto.getStoreId())
                .userId(dto.getUserId())
                .comment(dto.getComment())
                .score(dto.getScore())
                .build();
        return ReviewDto.fromEntity(reviewRepository.save(review));
    }

    /**
     * 리뷰 수정. 작성자 본인만 가능.
     */
    public ReviewDto updateReview(Long id, ReviewRequestDto dto) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("리뷰 없음"));
        // 작성자 본인 검증
        if (!Objects.equals(review.getUserId(), dto.getUserId())) {
            throw new ForbiddenException("수정 권한이 없습니다.");
        }
        validateScore(dto.getScore());
        review.setComment(dto.getComment());
        review.setScore(dto.getScore());
        return ReviewDto.fromEntity(reviewRepository.save(review));
    }

    /**
     * 리뷰 삭제. 작성자 본인만 가능.
     */
    public void deleteReview(Long id, String userId) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("리뷰 없음"));
        if (!Objects.equals(review.getUserId(), userId)) {
            throw new ForbiddenException("삭제 권한이 없습니다.");
        }
        reviewRepository.deleteById(id);
    }

    /**
     * 평점 범위 검증(1~5).
     */
    private void validateScore(Integer score) {
        if (score == null || score < 1 || score > 5) {
            throw new RuntimeException("평점은 1에서 5 사이여야 합니다.");
        }
    }
}
