package com.restaurant.reservation.controller;

import com.restaurant.reservation.dto.StoreDto;
import com.restaurant.reservation.service.StoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@CrossOrigin(origins = "*")
public class StoreController {

    private static final Logger logger = LoggerFactory.getLogger(StoreController.class);

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping
    public ResponseEntity<List<StoreDto>> getAllStores() {
        try {
            logger.info("모든 가게 목록 조회 요청");
            List<StoreDto> stores = storeService.getAllStores();
            logger.info("가게 목록 조회 완료: {}개", stores.size());
            return ResponseEntity.ok(stores);
        } catch (Exception e) {
            logger.error("가게 목록 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<StoreDto> getStoreById(@PathVariable String storeId) {
        try {
            logger.info("가게 조회 요청: storeId={}", storeId);
            StoreDto store = storeService.getStoreById(storeId);
            if (store != null) {
                logger.info("가게 조회 완료: storeId={}", storeId);
                return ResponseEntity.ok(store);
            } else {
                logger.warn("가게를 찾을 수 없음: storeId={}", storeId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("가게 조회 중 오류 발생: storeId={}", storeId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<StoreDto>> searchStores(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String location) {
        try {
            if (name != null && !name.trim().isEmpty()) {
                logger.info("가게명으로 검색 요청: name={}", name);
                List<StoreDto> stores = storeService.searchStoresByName(name);
                logger.info("가게명 검색 완료: {}개", stores.size());
                return ResponseEntity.ok(stores);
            } else if (location != null && !location.trim().isEmpty()) {
                logger.info("위치로 검색 요청: location={}", location);
                List<StoreDto> stores = storeService.searchStoresByLocation(location);
                logger.info("위치 검색 완료: {}개", stores.size());
                return ResponseEntity.ok(stores);
            } else {
                logger.warn("검색 조건이 없음");
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            logger.error("가게 검색 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 