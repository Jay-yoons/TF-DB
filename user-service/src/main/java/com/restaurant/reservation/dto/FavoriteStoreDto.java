package com.restaurant.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 즐겨찾기 가게 정보를 담는 DTO 클래스
 */
@AllArgsConstructor
@Setter
@Getter
@NoArgsConstructor
public class FavoriteStoreDto {
    private Long favStoreId;
    private String storeId;
    private String storeName;
    private String userId;
    private LocalDateTime createdAt;
}
