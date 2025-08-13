package com.example.store.service.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.store.service.entity.Store;
import com.example.store.service.entity.StoreSeat;
import com.example.store.service.repository.StoreRepository;
import com.example.store.service.repository.StoreSeatRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 스토어 도메인의 비즈니스 로직을 담당하는 서비스.
 * - 가게 조회/저장, 좌석 정보 조회
 * - 사용중 좌석 증감(낙관적 락 + 재시도) 및 여유 좌석 계산
 */
@Service
public class StoreService {
    private final StoreRepository repository;
    private final StoreSeatRepository storeSeatRepository;

    public StoreService(StoreRepository repository, StoreSeatRepository storeSeatRepository) {
        this.repository = repository;
        this.storeSeatRepository = storeSeatRepository;
    }

    /**
     * 모든 가게 목록을 반환한다.
     */
    public List<Store> getAllStores() {
        return repository.findAll();
    }

    /**
     * 카테고리 코드로 가게 목록을 조회한다.
     */
    public List<Store> getStoresByCategoryCode(Integer categoryCode) {
        if (categoryCode == null) {
            return getAllStores();
        }
        return repository.findByCategoryCode(categoryCode);
    }

    /**
     * 가게 식별자로 단건 조회한다.
     */
    public Store getStore(String storeId) {
        return repository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));
    }

    /**
     * 가게 저장/수정.
     */
    public Store saveStore(Store store) {
        return repository.save(store);
    }

    // 실시간 좌석 정보 조회 (Booking Service에서 업데이트)
    /**
     * 가게 좌석 정보를 조회한다. 없으면 기본값(0)으로 생성한다.
     */
    public StoreSeat getStoreSeatInfo(String storeId) {
        return storeSeatRepository.findByStoreId(storeId)
                .orElse(StoreSeat.builder()
                        .storeId(storeId)
                        .inUsingSeat(0)
                        .build());
    }

    /**
     * 여유 좌석 수를 반환한다. (전체 좌석 - 사용중 좌석)
     */
    public int getAvailableSeats(String storeId) {
        Store store = getStore(storeId);
        StoreSeat storeSeat = getStoreSeatInfo(storeId);
        return store.getSeatNum() - storeSeat.getInUsingSeat();
    }

    /**
     * 카테고리 코드 → 한글 카테고리명 매핑.
     */
    public String toKoreanCategoryName(Integer categoryCode) {
        com.example.store.service.entity.Category category = com.example.store.service.entity.Category.fromCode(categoryCode);
        return category != null ? category.getKoreanName() : "기타";
    }

    /**
     * 가게들을 카테고리명(한글) 기준으로 그룹핑한다.
     */
    public Map<String, List<Store>> groupStoresByKoreanCategoryName() {
        return getAllStores().stream()
                .collect(Collectors.groupingBy(s -> toKoreanCategoryName(s.getCategoryCode())));
    }

    // Booking Service에서 호출할 메서드
    /**
     * 좌석 정보 저장. (직접 세팅용)
     */
    public StoreSeat saveStoreSeat(StoreSeat storeSeat) {
        return storeSeatRepository.save(storeSeat);
    }

    // 예약 확정/취소 시 좌석 증감 처리
    /**
     * 예약 확정 시 사용중 좌석 수를 증가시킨다.
     */
    @Transactional
    public int incrementInUsingSeat(String storeId, int count) {
        return adjustInUsingSeat(storeId, Math.abs(count));
    }

    /**
     * 예약 취소 시 사용중 좌석 수를 감소시킨다.
     */
    @Transactional
    public int decrementInUsingSeat(String storeId, int count) {
        return adjustInUsingSeat(storeId, -Math.abs(count));
    }

    /**
     * 사용중 좌석 수 증감 처리(낙관적 락 충돌 시 최대 3회 재시도).
     */
    private int adjustInUsingSeat(String storeId, int delta) {
        Store store = getStore(storeId);
        StoreSeat storeSeat = getStoreSeatInfo(storeId);

        for (int retry = 0; retry < 3; retry++) {
            int current = storeSeat.getInUsingSeat();
            int next = current + delta;

            if (next < 0) {
                throw new IllegalArgumentException("사용중 좌석 수는 0 미만이 될 수 없습니다.");
            }
            if (next > store.getSeatNum()) {
                throw new IllegalArgumentException("사용중 좌석 수가 전체 좌석 수를 초과할 수 없습니다.");
            }

            storeSeat.setInUsingSeat(next);
            try {
                storeSeatRepository.saveAndFlush(storeSeat);
                return store.getSeatNum() - next; // 성공 시 여유 좌석 수 반환
            } catch (org.springframework.dao.OptimisticLockingFailureException e) {
                // 버전 충돌 시 재조회 후 재시도
                storeSeat = storeSeatRepository.findByStoreId(storeId).orElseThrow();
            }
        }

        throw new IllegalStateException("좌석 증감 처리 중 충돌이 발생했습니다. 다시 시도해주세요.");
    }
}
