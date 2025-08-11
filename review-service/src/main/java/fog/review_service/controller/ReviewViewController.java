package fog.review_service.controller;

import fog.review_service.dto.ReviewDto;
import fog.review_service.dto.ReviewRequestDto;
import fog.review_service.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewViewController {

    private final ReviewService reviewService;

    // 기본 접근 시 리다이렉트
    @GetMapping("")
    public String redirectToDefaultStore() {
        return "redirect:/reviews/stores/STORE001";
    }

    // 가게별 모든 리뷰 목록 조회 (가게 페이지)
    @GetMapping("/stores/{storeId}")
    public String getStoreReviews(@PathVariable String storeId, Model model) {
        List<ReviewDto> reviews = reviewService.getStoreReviews(storeId);
        model.addAttribute("reviews", reviews);
        model.addAttribute("storeId", storeId);
        model.addAttribute("title", storeId + " 리뷰 목록");
        return "store-reviews";
    }

    // 특정 가게의 특정 사용자 리뷰 조회 (개인 리뷰 확인용)
    @GetMapping("/stores/{storeId}/users/{userId}")
    public String getUserReviewByStore(@PathVariable String storeId, 
                                       @PathVariable String userId, 
                                       Model model) {
        List<ReviewDto> reviews = reviewService.getMyReviewsByStore(userId, storeId);
        model.addAttribute("reviews", reviews);
        model.addAttribute("storeId", storeId);
        model.addAttribute("userId", userId);
        model.addAttribute("title", userId + "의 " + storeId + " 리뷰");
        return "list";
    }

    // 특정 사용자의 모든 리뷰 목록 조회
    @GetMapping("/users/{userId}")
    public String getUserReviews(@PathVariable String userId, Model model) {
        List<ReviewDto> reviews = reviewService.getMyReviews(userId);
        model.addAttribute("reviews", reviews);
        model.addAttribute("userId", userId);
        model.addAttribute("title", userId + "의 모든 리뷰");
        return "list";
    }

    // 리뷰 작성 폼 (가게 페이지에서 접근)
    @GetMapping("/stores/{storeId}/new")
    public String newReviewForm(@PathVariable String storeId, Model model) {
        model.addAttribute("review", new ReviewRequestDto(storeId, "", 5)); // 기본값 5점
        model.addAttribute("storeId", storeId);
        model.addAttribute("title", storeId + " 리뷰 작성");
        return "form";
    }

    // 리뷰 생성 (사용자 ID는 폼에서 입력받음)
    @PostMapping("/stores/{storeId}")
    public String createReview(@PathVariable String storeId,
                              @ModelAttribute ReviewRequestDto dto,
                              @RequestParam String userId) {
        reviewService.createReview(storeId, userId, dto);
        return "redirect:/reviews/stores/" + storeId;
    }

    // 리뷰 수정 폼
    @GetMapping("/{reviewId}/edit")
    public String editReviewForm(@PathVariable Long reviewId, Model model) {
        ReviewDto review = reviewService.getReview(reviewId);
        model.addAttribute("review", review);
        model.addAttribute("title", "리뷰 수정");
        return "form";
    }

    // 리뷰 수정
    @PutMapping("/{reviewId}")
    public String updateReview(@PathVariable Long reviewId,
                              @ModelAttribute ReviewRequestDto dto) {
        ReviewDto updatedReview = reviewService.updateReview(reviewId, dto);
        return "redirect:/reviews/stores/" + updatedReview.getStoreId();
    }

    // 리뷰 삭제 (RESTful하게 DELETE 메서드 사용)
    @DeleteMapping("/{reviewId}")
    public String deleteReview(@PathVariable Long reviewId, 
                              @RequestParam String storeId) {
        reviewService.deleteReview(reviewId);
        return "redirect:/reviews/stores/" + storeId;
    }

    // POST로 삭제하는 대체 메서드 (HTML 폼에서 DELETE를 지원하지 않는 경우)
    @PostMapping("/{reviewId}/delete")
    public String deleteReviewPost(@PathVariable Long reviewId, 
                                  @RequestParam String storeId) {
        reviewService.deleteReview(reviewId);
        return "redirect:/reviews/stores/" + storeId;
    }
}
