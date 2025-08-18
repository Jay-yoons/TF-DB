package com.example.store.service.dto;

import lombok.*;

/**
 * 리뷰 작성/수정 요청 DTO.
 * - reviewId: 수정 시 사용, 생성 시 null
 * - storeId: 가게 식별자
 * - comment: 리뷰 내용(최대 50자)
 * - score: 평점(1~5)
 *
 * [중요] 사용자 식별자(userId)는 클라이언트에서 받지 않으며,
 *        서버가 Cognito JWT(sub)에서 추출한다.
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDto {
    private Long reviewId;
    private String storeId;
    private String storeName;
    private String comment;
    private Integer score;
}
