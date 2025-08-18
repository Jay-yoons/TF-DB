package com.example.store.service.controller;

import com.example.store.service.dto.ReviewDto;
import com.example.store.service.dto.ReviewRequestDto;
import com.example.store.service.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 리뷰 관련 REST API 엔드포인트.
 * - 사용자 식별자는 Cognito JWT의 sub 클레임을 사용한다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    // 특정 가게의 모든 리뷰
    @GetMapping("/stores/{storeId}")
    public List<ReviewDto> getStoreReviews(@PathVariable String storeId) {
        return reviewService.getStoreReviews(storeId);
    }

    // 내 모든 리뷰
    @GetMapping("/my")
    public List<ReviewDto> getMyReviews(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaimAsString("sub");
        return reviewService.getMyReviews(userId);
    }

    // 특정 매장에서의 내 리뷰
    @GetMapping("/my/stores/{storeId}")
    public List<ReviewDto> getMyReviewsByStore(@AuthenticationPrincipal Jwt jwt, @PathVariable String storeId) {
        String userId = jwt.getClaimAsString("sub");
        return reviewService.getMyReviewsByStore(userId, storeId);
    }

    // [별칭] 내 모든 리뷰 (설계안: GET /reviews)
    @GetMapping
    public List<ReviewDto> getMyReviewsAlias(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaimAsString("sub");
        return reviewService.getMyReviews(userId);
    }

    // 리뷰 단건
    @GetMapping("/{id}")
    public ReviewDto getReview(@PathVariable Long id) {
        return reviewService.getReview(id);
    }

    // 리뷰 작성
    @PostMapping
    public ReviewDto createReview(@AuthenticationPrincipal Jwt jwt, @RequestBody ReviewRequestDto dto) {
        String userId = jwt.getClaimAsString("sub");
        return reviewService.createReview(userId, dto);
    }

    // 리뷰 수정
    @PutMapping("/{id}")
    public ReviewDto updateReview(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt, @RequestBody ReviewRequestDto dto) {
        String userId = jwt.getClaimAsString("sub");
        return reviewService.updateReview(id, userId, dto);
    }

    // 리뷰 삭제
    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaimAsString("sub");
        reviewService.deleteReview(id, userId);
    }
}
