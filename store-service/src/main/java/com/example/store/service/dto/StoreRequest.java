package com.example.store.service.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 가게 등록/수정 요청 DTO.
 * - storeName: 가게 이름(최대 50자)
 * - categoryCode: 카테고리 코드(Integer)
 * - storeLocation: 가게 주소(최대 50자)
 * - seatNum: 전체 좌석 수
 * - serviceTime: 영업시간 문자열(예: "09:00~21:00")
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreRequest {
    private String storeName;
    private Integer categoryCode;
    private String storeLocation;
    private int seatNum;
    private String serviceTime;
}
