package com.example.store.service.controller;

import com.example.store.service.dto.ReviewDto;
import com.example.store.service.dto.ReviewRequestDto;
import com.example.store.service.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 리뷰 관련 REST API 엔드포인트 집합.
 * - 현재는 로그인 연동 전이므로 userId를 요청에서 받지만,
 *   Cognito 연동 이후에는 토큰 클레임에서 사용자 식별자를 추출하도록 변경 예정.
 * - 수정/삭제는 서비스 레이어에서 작성자 본인 여부를 검증한다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 특정 가게의 모든 리뷰 목록을 조회한다.
     */
    @GetMapping("/stores/{storeId}")
    public List<ReviewDto> getStoreReviews(@PathVariable String storeId) {
        log.info("리뷰 컨트롤러 진입");
        return reviewService.getStoreReviews(storeId);
    }

    /**
     * 작성자 본인의 모든 리뷰를 조회한다.
     * Cognito 연동 후에는 userId 파라미터 없이 토큰에서 추출할 예정이다.
     */
    @GetMapping("/my")
    public List<ReviewDto> getMyReviews(@RequestParam String userId) {
        return reviewService.getMyReviews(userId);
    }

    /**
     * 특정 가게에서 작성자 본인이 작성한 리뷰를 조회한다.
     */
    @GetMapping("/my/stores/{storeId}")
    public List<ReviewDto> getMyReviewsByStore(@RequestParam String userId, @PathVariable String storeId) {
        return reviewService.getMyReviewsByStore(userId, storeId);
    }

    /**
     * 리뷰 단건 조회.
     */
    @GetMapping("/{id}")
    public ReviewDto getReview(@PathVariable Long id) {
        return reviewService.getReview(id);
    }

    /**
     * 리뷰 작성. 동일 사용자/가게 중복은 허용하지 않는다.
     */
    @PostMapping
    public ReviewDto createReview(@RequestBody ReviewRequestDto dto) {
        return reviewService.createReview(dto);
    }

    /**
     * 리뷰 수정. 작성자 본인만 가능.
     */
    @PutMapping("/{id}")
    public ReviewDto updateReview(@PathVariable Long id, @RequestBody ReviewRequestDto dto) {
        return reviewService.updateReview(id, dto);
    }

    /**
     * 리뷰 삭제. 작성자 본인만 가능.
     */
    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Long id, @RequestParam String userId) {
        reviewService.deleteReview(id, userId);
    }
}
