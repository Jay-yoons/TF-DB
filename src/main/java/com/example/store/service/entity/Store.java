package com.example.store.service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "store")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store {
    @Id
    @Column(name = "STORE_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long storeId;

    @Column(name = "STORE_NAME")
    private String storeName;
    
    @Column(name = "CATEGORY_CODE")
    private String categoryCode;
    
    @Column(name = "STORE_LOCATION")
    private String storeLocation; // 예: "서울 강남구"
    
    @Column(name = "SEAT_NUM")
    private int seatNum;
    
    @Column(name = "SERVICE_TIME")
    private String serviceTime; // 영업시간 문자열 예: "09:00~21:00"

    // 여유좌석, 대기시간 등은 실시간이므로 별도 서비스에서 관리 예정
}
