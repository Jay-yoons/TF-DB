package com.restaurant.reservation.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 즐겨찾기 가게 정보를 담는 DTO 클래스
 */
@Getter
@Setter
public class FavoriteStoreDto {
    private Long favStoreId;
    private String storeId;
    private String storeName;
    private String userId;

    // 기본 생성자
    public FavoriteStoreDto() {}

    // 전체 생성자
    public FavoriteStoreDto(Long favStoreId, String storeId, String storeName, String userId) {
        this.favStoreId = favStoreId;
        this.storeId = storeId;
        this.storeName = storeName;
        this.userId = userId;
    }
}
