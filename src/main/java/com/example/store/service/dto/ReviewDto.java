package com.example.store.service.dto;

import com.example.store.service.entity.Review;
import lombok.*;

/**
 * 리뷰 조회용 응답 DTO.
 * - reviewId: 리뷰 식별자(PK)
 * - storeId: 가게 식별자(STORES.STORE_ID)
 * - userId: 작성자 식별자(로그인 사용자)
 * - comment: 리뷰 내용(최대 50자)
 * - score: 평점(1~5)
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDto {
    private Long reviewId;
    private String storeId;
    private String storeName;
    private String userId;
    private String comment;
    private Integer score;

    public static ReviewDto fromEntity(Review review) {
        return ReviewDto.builder()
                .reviewId(review.getReviewId())
                .storeId(review.getStoreId())
                .storeName(review.getStoreName())
                .userId(review.getUserId())
                .comment(review.getComment())
                .score(review.getScore())
                .build();
    }
}
