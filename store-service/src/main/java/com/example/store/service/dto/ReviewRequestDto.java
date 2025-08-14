package com.example.store.service.dto;

import lombok.*;

/**
 * 리뷰 작성/수정 요청 DTO.
 * - reviewId: 수정 시 사용, 생성 시 null
 * - storeId: 가게 식별자
 * - userId: 작성자 식별자(로그인 사용자)
 * - comment: 리뷰 내용(최대 50자)
 * - score: 평점(1~5)
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDto {
    private Long reviewId;
    private String storeId;
    private String userId;
    private String comment;
    private Integer score;
}
