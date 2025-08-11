package fog.review_service.dto;

import fog.review_service.entity.Review;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ReviewDto {
    private Long reviewId;
    private String storeId;
    private String userId;
    private String comment;
    private Integer score;
    private String userName; // 사용자 이름 필드 추가

    public static ReviewDto fromEntity(Review review) {
        return ReviewDto.builder()
                .reviewId(review.getReviewId())
                .storeId(review.getStoreId())
                .userId(review.getUserId())
                .comment(review.getComment())
                .score(review.getScore())
                .build();
    }

    public static ReviewDto fromEntity(Review review, UserInfoDto userInfo) {
        return ReviewDto.builder()
                .reviewId(review.getReviewId())
                .storeId(review.getStoreId())
                .userId(review.getUserId())
                .comment(review.getComment())
                .score(review.getScore())
                .userName(userInfo != null ? userInfo.getUserName() : null)
                .build();
    }
}
