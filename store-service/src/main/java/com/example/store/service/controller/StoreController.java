package com.example.store.service.controller;

import com.example.store.service.entity.StoreSeat;
import org.springframework.web.bind.annotation.*;
import com.example.store.service.entity.Store;
import com.example.store.service.service.StoreService;
import com.example.store.service.dto.StoreResponse;

import java.util.List;

/**
 * 스토어 관련 REST API 엔드포인트 집합.
 * - 목록/상세/여유 좌석 조회
 * - 좌석 직접 세팅 및 예약 연동을 위한 증감 API 제공
 */
@RestController
@RequestMapping("/api/stores")
public class StoreController {

    private final StoreService service;

    public StoreController(StoreService service) {
        this.service = service;
    }

    /** 가게 목록 API */
    @GetMapping
    public List<StoreResponse> listStores() {
        return service.getAllStores().stream()
                .map(StoreResponse::fromEntity)
                .toList();
    }

    /** 가게 상세 API */
    @GetMapping("/{storeId}")
    public StoreResponse storeDetail(@PathVariable String storeId) {
        Store store = service.getStore(storeId);
        int availableSeats = service.getAvailableSeats(storeId);
        return StoreResponse.fromEntityWithSeats(store, availableSeats);
    }

    /** Booking Service에서 호출할 REST API (좌석 정보 업데이트) */
    @PutMapping("/{storeId}/seats")
    public String updateSeatInfo(@PathVariable String storeId, @RequestParam int inUsingSeat) {
        StoreSeat storeSeat = service.getStoreSeatInfo(storeId);
        storeSeat.setInUsingSeat(inUsingSeat);
        service.saveStoreSeat(storeSeat);
        return "좌석 정보가 업데이트되었습니다.";
    }

    /** Booking Service에서 호출할 REST API (여유 좌석 조회) */
    @GetMapping("/{storeId}/available-seats")
    public int getAvailableSeats(@PathVariable String storeId) {
        return service.getAvailableSeats(storeId);
    }

    /** 예약 확정 시: 사용중 좌석 증가 */
    @PostMapping("/{storeId}/seats/increment")
    public int incrementInUsingSeat(@PathVariable String storeId, @RequestParam(defaultValue = "1") int count) {
        return service.incrementInUsingSeat(storeId, count);
    }

    /** 예약 취소 시: 사용중 좌석 감소 */
    @PostMapping("/{storeId}/seats/decrement")
    public int decrementInUsingSeat(@PathVariable String storeId, @RequestParam(defaultValue = "1") int count) {
        return service.decrementInUsingSeat(storeId, count);
    }
}
