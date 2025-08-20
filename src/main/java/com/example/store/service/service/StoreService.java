package com.example.store.service.service;

import com.example.store.service.entity.StoreNameMapping;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.store.service.entity.Store;
import com.example.store.service.entity.StoreSeat;
import com.example.store.service.repository.StoreRepository;
import com.example.store.service.repository.StoreSeatRepository;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 스토어 도메인의 비즈니스 로직을 담당하는 서비스.
 * - 가게 조회/저장, 좌석 정보 조회
 * - 사용중 좌석 증감(낙관적 락 + 재시도) 및 여유 좌석 계산
 * - 영업시간 문자열 파싱을 통한 현재 영업 상태 계산
 */
@Service
@Slf4j
public class StoreService {
    private final StoreRepository repository;
    private final StoreSeatRepository storeSeatRepository;
    private final com.example.store.service.repository.StoreLocationRepository storeLocationRepository;

    public StoreService(StoreRepository repository,
                        StoreSeatRepository storeSeatRepository,
                        com.example.store.service.repository.StoreLocationRepository storeLocationRepository) {
        this.repository = repository;
        this.storeSeatRepository = storeSeatRepository;
        this.storeLocationRepository = storeLocationRepository;
    }

    @Transactional
    public String getStoreName(String storeId) {
        StoreNameMapping storeName = repository.findByStoreId(storeId);
        log.info("storeNAme={}", storeName.getStoreName());
        return storeName.getStoreName();
    }

    public List<Store> getAllStores() { return repository.findAll(); }

    public List<Store> getStoresByCategoryCode(Integer categoryCode) {
        if (categoryCode == null) return getAllStores();
        return repository.findByCategoryCode(categoryCode);
    }

    public Store getStore(String storeId) {
        return repository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));
    }

    /** 위경도 조회 (없으면 null) */
    public com.example.store.service.entity.StoreLocation getStoreLocation(String storeId) {
        return storeLocationRepository.findByStoreId(storeId).orElse(null);
    }

    public Store saveStore(Store store) { return repository.save(store); }

    // 좌석
    public StoreSeat getStoreSeatInfo(String storeId) {
        return storeSeatRepository.findByStoreId(storeId)
                .orElse(StoreSeat.builder().storeId(storeId).inUsingSeat(0).build());
    }
    public int getAvailableSeats(String storeId) {
        Store store = getStore(storeId);
        StoreSeat storeSeat = getStoreSeatInfo(storeId);
        return store.getSeatNum() - storeSeat.getInUsingSeat();
    }

    // 카테고리
    public String toKoreanCategoryName(Integer categoryCode) {
        com.example.store.service.entity.Category category = com.example.store.service.entity.Category.fromCode(categoryCode);
        return category != null ? category.getKoreanName() : "기타";
    }
    public Map<String, List<Store>> groupStoresByKoreanCategoryName() {
        return getAllStores().stream()
                .collect(Collectors.groupingBy(s -> toKoreanCategoryName(s.getCategoryCode())));
    }

    // 저장
    public StoreSeat saveStoreSeat(StoreSeat storeSeat) { return storeSeatRepository.save(storeSeat); }

    @Transactional
    public int incrementInUsingSeat(String storeId, int count) { return adjustInUsingSeat(storeId, Math.abs(count)); }

    @Transactional
    public int decrementInUsingSeat(String storeId, int count) { return adjustInUsingSeat(storeId, -Math.abs(count)); }

    private int adjustInUsingSeat(String storeId, int delta) {
        Store store = getStore(storeId);
        StoreSeat storeSeat = getStoreSeatInfo(storeId);

        int current = storeSeat.getInUsingSeat();
        int next = current + delta;

        if (next < 0) throw new IllegalArgumentException("사용중 좌석 수는 0 미만이 될 수 없습니다.");
        if (next > store.getSeatNum()) throw new IllegalArgumentException("사용중 좌석 수가 전체 좌석 수를 초과할 수 없습니다.");

        storeSeat.setInUsingSeat(next);
        storeSeatRepository.saveAndFlush(storeSeat);
        return store.getSeatNum() - next;
    }

    // ===================== 영업시간 판단 로직 =====================

    /** 현재(Asia/Seoul) 기준 영업중 여부. */
    public boolean isOpenNow(Store store) {
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Seoul"));
        // OPEN/CLOSE 값이 모두 있을 때만 판단, 없으면 영업상태 판단 불가(false)
        if (store.getOpenTime() != null && store.getCloseTime() != null) {
            return isOpenAt(store.getOpenTime(), store.getCloseTime(), now);
        }
        return false;
    }

    /** 현재 상태 라벨 ("영업중"/"영업종료"). */
    public String openStatus(Store store) {
        return isOpenNow(store) ? "영업중" : "영업종료";
    }

    /** 단일 구간(open~close) 기준 영업중 여부 계산. 24시간/심야 구간 포함 */
    boolean isOpenAt(LocalTime open, LocalTime close, LocalTime now) {
        if (open == null || close == null) return false;
        if (open.equals(close)) return true; // 24시간
        if (close.isAfter(open)) {
            return !now.isBefore(open) && now.isBefore(close);
        } else {
            // 심야 구간 (예: 22:00~02:00)
            return !now.isBefore(open) || now.isBefore(close);
        }
    }

    // service_time 문자열 파서 제거: OPEN_TIME/CLOSE_TIME 컬럼만 사용합니다.
}
