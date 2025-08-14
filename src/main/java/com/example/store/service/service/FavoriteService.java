// Java
// [신규] 즐겨찾기 서비스 - service 패키지 “바로 아래”에 생성하세요.
package com.example.store.service.service;

import com.example.store.service.entity.FavStore;
import com.example.store.service.entity.Store;
import com.example.store.service.exception.BadRequestException;
import com.example.store.service.exception.NotFoundException;
import com.example.store.service.repository.FavStoreRepository;
import com.example.store.service.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 즐겨찾기 비즈니스 로직.
 * - 생성 시 storeId로 Store 조회 → storeName 세팅 (클라이언트 입력값은 신뢰하지 않음)
 * - 중복 방지, 삭제, 사용자 목록 조회
 */
@Service
@RequiredArgsConstructor
public class FavoriteService { // [중요] 파일명은 FavoriteService.java

    private final FavStoreRepository favStoreRepository;
    private final StoreRepository storeRepository;

    /**
     * 즐겨찾기 추가.
     * - 동일 사용자/가게 중복 방지
     * - STORE_NAME은 Store에서 조회해 비정규화로 저장
     */
    public FavStore addFavorite(String userId, String storeId) {
        if (favStoreRepository.findByStoreIdAndUserId(storeId, userId).isPresent()) {
            throw new BadRequestException("이미 즐겨찾기에 추가된 매장입니다.");
        }
        // [중요] storeId 유효성 및 매장명 확보
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 매장입니다."));

        FavStore fav = FavStore.builder()
                .userId(userId)
                .storeId(storeId)
                .storeName(store.getStoreName()) // [비정규화] 스냅샷 저장
                .build();

        return favStoreRepository.save(fav);
    }

    /**
     * 즐겨찾기 제거.
     */
    public void removeFavorite(String userId, String storeId) {
        FavStore fav = favStoreRepository.findByStoreIdAndUserId(storeId, userId)
                .orElseThrow(() -> new NotFoundException("즐겨찾기에 없습니다."));
        favStoreRepository.delete(fav);
    }

    /**
     * 나의 즐겨찾기 목록.
     */
    public List<FavStore> getMyFavorites(String userId) {
        return favStoreRepository.findByUserId(userId);
    }
}
