package fog.review_service.controller;

import fog.review_service.dto.ReviewDto;
import fog.review_service.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/test")
public class TestController {

    private final ReviewService reviewService;

    public TestController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // /test 기본 경로 핸들러 추가
    @GetMapping("")
    public String testHome() {
        return "redirect:/test/links";
    }

    // 모든 리뷰 조회 (테스트용)
    @GetMapping("/all-reviews")
    public String getAllReviews(Model model) {
        List<ReviewDto> reviews = reviewService.getReviewsWithUser();
        model.addAttribute("reviews", reviews);
        model.addAttribute("title", "모든 리뷰 목록 (테스트용)");
        return "list";
    }

    // 테스트 페이지 (다양한 링크 제공)
    @GetMapping("/links")
    public String testLinks(Model model) {
        return "test-links";
    }
}
