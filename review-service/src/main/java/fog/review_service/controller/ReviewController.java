package fog.review_service.controller;

import fog.review_service.dto.ReviewDto;
import fog.review_service.dto.ReviewRequestDto;
import fog.review_service.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;


    @GetMapping
    public List<ReviewDto> getMyReviews(@RequestParam String userId) {
        return reviewService.getMyReviews(userId);
    }

    @GetMapping("/{id}")
    public ReviewDto getReview(@PathVariable Long id) {
        return reviewService.getReview(id);
    }

    @PostMapping("/stores/{storeId}")
    public ReviewDto createReview(@PathVariable String storeId,
                                  @RequestParam String userId,
                                  @RequestBody ReviewRequestDto dto) {
        return reviewService.createReview(storeId, userId, dto);
    }

    @PutMapping("/{id}")
    public ReviewDto updateReview(@PathVariable Long id,
                                  @RequestBody ReviewRequestDto dto) {
        return reviewService.updateReview(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
    }
}
