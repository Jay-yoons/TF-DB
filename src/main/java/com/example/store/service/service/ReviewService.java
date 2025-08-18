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
 * 리뷰 비즈니스 로직.
 * - 사용자 식별자는 클라이언트가 주지 않고(JWT sub 사용) 서비스 인자로 전달받는다.
 * - 평점 범위(1~5) 검증, 중복 작성 방지, 본인 권한 검증 포함.
 */
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    // storeId -> storeName 조회용
    private final com.example.store.service.repository.StoreRepository storeRepository;

    // 특정 가게의 리뷰 목록
    public List<ReviewDto> getStoreReviews(String storeId) {
        return reviewRepository.findByStoreId(storeId).stream()
                .map(ReviewDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 내 모든 리뷰
    public List<ReviewDto> getMyReviews(String userId) {
        return reviewRepository.findByUserId(userId).stream()
                .map(ReviewDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 특정 가게에서의 내 리뷰
    public List<ReviewDto> getMyReviewsByStore(String userId, String storeId) {
        return reviewRepository.findByStoreId(storeId).stream()
                .filter(r -> r.getUserId().equals(userId))
                .map(ReviewDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 리뷰 단건
    public ReviewDto getReview(Long id) {
        return reviewRepository.findById(id)
                .map(ReviewDto::fromEntity)
                .orElseThrow(() -> new NotFoundException("리뷰가 없습니다."));
    }

    // 리뷰 작성
    public ReviewDto createReview(String userId, ReviewRequestDto dto) {
        validateScore(dto.getScore());
        // storeId 유효성 및 최신 매장명 확보
        com.example.store.service.entity.Store store = storeRepository.findById(dto.getStoreId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 매장입니다."));
        if (reviewRepository.findByStoreIdAndUserId(dto.getStoreId(), userId).isPresent()) {
            throw new BadRequestException("이미 작성한 리뷰입니다.");
        }
        Review review = Review.builder()
                .storeId(dto.getStoreId())
                .userId(userId)
                .comment(dto.getComment())
                .score(dto.getScore())
                .build();
        // 비정규화 컬럼 세팅
        review.setStoreName(store.getStoreName());
        return ReviewDto.fromEntity(reviewRepository.save(review));
    }

    // 리뷰 수정(작성자 본인만)
    public ReviewDto updateReview(Long id, String userId, ReviewRequestDto dto) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("리뷰 없음"));
        if (!Objects.equals(review.getUserId(), userId)) {
            throw new ForbiddenException("수정 권한이 없습니다.");
        }
        validateScore(dto.getScore());
        review.setComment(dto.getComment());
        review.setScore(dto.getScore());
        return ReviewDto.fromEntity(reviewRepository.save(review));
    }

    // 리뷰 삭제(작성자 본인만)
    public void deleteReview(Long id, String userId) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("리뷰 없음"));
        if (!Objects.equals(review.getUserId(), userId)) {
            throw new ForbiddenException("삭제 권한이 없습니다.");
        }
        reviewRepository.deleteById(id);
    }

    // 평점 범위 검증(1~5)
    private void validateScore(Integer score) {
        if (score == null || score < 1 || score > 5) {
            throw new RuntimeException("평점은 1에서 5 사이여야 합니다.");
        }
    }
}
