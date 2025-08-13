package com.example.store.service.controller;

import com.example.store.service.entity.StoreSeat;
import org.springframework.web.bind.annotation.*;
import com.example.store.service.entity.Store;
import com.example.store.service.service.StoreService;
import com.example.store.service.service.StoreImageService;
import com.example.store.service.dto.StoreResponse;

import java.util.List;
import java.util.Map;

/**
 * 스토어 관련 REST API 엔드포인트 집합.
 * - 목록/상세/여유 좌석 조회
 * - 좌석 직접 세팅 및 예약 연동을 위한 증감 API 제공
 */
@RestController
@RequestMapping("/api/stores")
public class StoreController {

    private final StoreService service;
    private final StoreImageService imageService;

    public StoreController(StoreService service, StoreImageService imageService) {
        this.service = service;
        this.imageService = imageService;
    }

    /** 가게 목록 API (옵션: categoryCode로 필터링) */
    @GetMapping
    public List<StoreResponse> listStores(@RequestParam(value = "categoryCode", required = false) Integer categoryCode) {
        return service.getStoresByCategoryCode(categoryCode).stream()
                .map(StoreResponse::fromEntity)
                .peek(r -> {
                    r.setImageUrl(imageService.getImageUrl(r.getStoreId()));
                    r.setImageUrls(imageService.listImageUrls(r.getStoreId(), 10));
                })
                .toList();
    }

    /** 가게 상세 API */
    @GetMapping("/{storeId}")
    public StoreResponse storeDetail(@PathVariable String storeId) {
        Store store = service.getStore(storeId);
        int availableSeats = service.getAvailableSeats(storeId);
        StoreResponse response = StoreResponse.fromEntityWithSeats(store, availableSeats);
        response.setImageUrl(imageService.getImageUrl(response.getStoreId()));
        response.setImageUrls(imageService.listImageUrls(response.getStoreId(), 10));
        return response;
    }

    /** 가게 위치(위경도) 전용 API */
    @GetMapping("/{storeId}/location")
    public Map<String, String> getStoreLocation(@PathVariable String storeId) {
        Store store = service.getStore(storeId);
        return Map.of(
                "latitude", store.getLatitude(),
                "longitude", store.getLongitude()
        );
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

    /** 가게 목록을 카테고리명(한식/일식/양식/중식/카페)으로 그룹핑하여 반환 */
    @GetMapping("/group-by-category")
    public Map<String, List<StoreResponse>> groupByCategory() {
        return service.groupStoresByKoreanCategoryName().entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        java.util.Map.Entry::getKey,
                        e -> e.getValue().stream().map(StoreResponse::fromEntity)
                                .peek(r -> {
                                    r.setImageUrl(imageService.getImageUrl(r.getStoreId()));
                                    r.setImageUrls(imageService.listImageUrls(r.getStoreId(), 10));
                                })
                                .toList()
                ));
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
