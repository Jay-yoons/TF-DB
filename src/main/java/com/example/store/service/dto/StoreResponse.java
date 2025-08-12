package com.example.store.service.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 가게 조회 응답 DTO.
 * - storeId: 가게 식별자(STORES.STORE_ID)
 * - storeName: 가게 이름
 * - categoryCode: 카테고리 코드(Integer)
 * - storeLocation: 가게 주소
 * - seatNum: 전체 좌석 수
 * - serviceTime: 영업시간 문자열(예: "09:00~21:00")
 * - availableSeats: 여유 좌석 수(= 전체 좌석 - 사용중 좌석)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreResponse {
    private String storeId;
    private String storeName;
    private Integer categoryCode;
    private String storeLocation;
    private int seatNum;
    private String serviceTime;
    private Integer availableSeats; // 선택 응답 필드

    public static StoreResponse fromEntity(com.example.store.service.entity.Store store) {
        return StoreResponse.builder()
                .storeId(store.getStoreId())
                .storeName(store.getStoreName())
                .categoryCode(store.getCategoryCode())
                .storeLocation(store.getStoreLocation())
                .seatNum(store.getSeatNum())
                .serviceTime(store.getServiceTime())
                .build();
    }

    // (선택) 좌석 정보까지 포함하는 변환 메서드
    public static StoreResponse fromEntityWithSeats(com.example.store.service.entity.Store store, int availableSeats) {
        StoreResponse response = fromEntity(store);
        response.setAvailableSeats(availableSeats);
        return response;
    }
}
