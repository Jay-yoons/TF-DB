package com.restaurant.reservation.controller;

import com.restaurant.reservation.dto.favorite.FavoriteRequestDto;
import com.restaurant.reservation.dto.favorite.FavoriteResponseDto;
import com.restaurant.reservation.service.FavoriteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorites")
@CrossOrigin(origins = "*")
public class FavoriteController {
    
    private static final Logger logger = LoggerFactory.getLogger(FavoriteController.class);
    
    private final FavoriteService favoriteService;
    
    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }
    
    /**
     * 내 즐겨찾기 가게 목록 조회 (JWT 기반)
     */
    @GetMapping
    public ResponseEntity<List<FavoriteResponseDto>> getMyFavorites() {
        try {
            logger.info("내 즐겨찾기 가게 목록 조회 요청");
            
            String userId = getCurrentUserId();
            if (userId == null) {
                logger.warn("로그인이 필요합니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            List<FavoriteResponseDto> favorites = favoriteService.getUserFavorites(userId);
            
            logger.info("내 즐겨찾기 가게 목록 조회 완료: {}개", favorites.size());
            return ResponseEntity.ok(favorites);
            
        } catch (Exception e) {
            logger.error("내 즐겨찾기 가게 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 즐겨찾기 추가 (JWT 기반)
     */
    @PostMapping
    public ResponseEntity<FavoriteResponseDto> addFavorite(@RequestBody FavoriteRequestDto requestDto) {
        try {
            logger.info("즐겨찾기 추가 요청: storeId={}", requestDto.getStoreId());
            
            String userId = getCurrentUserId();
            if (userId == null) {
                logger.warn("로그인이 필요합니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            requestDto.setUserId(userId);
            FavoriteResponseDto response = favoriteService.addFavorite(requestDto);
            
            logger.info("즐겨찾기 추가 완료: favStoreId={}", response.getFavStoreId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            logger.error("즐겨찾기 추가 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 즐겨찾기 삭제 (JWT 기반)
     */
    @DeleteMapping("/{storeId}")
    public ResponseEntity<Void> removeFavorite(@PathVariable String storeId) {
        try {
            logger.info("즐겨찾기 삭제 요청: storeId={}", storeId);
            
            String userId = getCurrentUserId();
            if (userId == null) {
                logger.warn("로그인이 필요합니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            favoriteService.removeFavorite(userId, storeId);
            
            logger.info("즐겨찾기 삭제 완료: userId={}, storeId={}", userId, storeId);
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            logger.error("즐겨찾기 삭제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 특정 가게의 즐겨찾기 여부 확인 (JWT 기반)
     */
    @GetMapping("/check/{storeId}")
    public ResponseEntity<Boolean> checkFavorite(@PathVariable String storeId) {
        try {
            logger.info("즐겨찾기 여부 확인 요청: storeId={}", storeId);
            
            String userId = getCurrentUserId();
            if (userId == null) {
                logger.warn("로그인이 필요합니다");
                return ResponseEntity.ok(false);
            }
            
            boolean isFavorite = favoriteService.isFavorite(userId, storeId);
            
            logger.info("즐겨찾기 여부 확인 완료: storeId={}, isFavorite={}", storeId, isFavorite);
            return ResponseEntity.ok(isFavorite);
            
        } catch (Exception e) {
            logger.error("즐겨찾기 여부 확인 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 사용자의 즐겨찾기 가게 ID 목록 조회 (JWT 기반)
     */
    @GetMapping("/store-ids")
    public ResponseEntity<List<String>> getUserFavoriteStoreIds() {
        try {
            logger.info("사용자 즐겨찾기 가게 ID 목록 조회 요청");
            
            String userId = getCurrentUserId();
            if (userId == null) {
                logger.warn("로그인이 필요합니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            List<String> storeIds = favoriteService.getUserFavoriteStoreIds(userId);
            
            logger.info("사용자 즐겨찾기 가게 ID 목록 조회 완료: {}개", storeIds.size());
            return ResponseEntity.ok(storeIds);
            
        } catch (Exception e) {
            logger.error("즐겨찾기 가게 ID 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 현재 인증된 사용자의 ID를 가져오는 헬퍼 메서드
     */
    private String getCurrentUserId() {
        try {
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getName())) {
                return authentication.getName();
            }
            
            return null;
        } catch (Exception e) {
            logger.error("현재 사용자 ID 조회 중 오류 발생", e);
            return null;
        }
    }
}
