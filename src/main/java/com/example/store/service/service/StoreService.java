package com.example.store.service.service;

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
public class StoreService {
    private final StoreRepository repository;
    private final StoreSeatRepository storeSeatRepository;

    public StoreService(StoreRepository repository, StoreSeatRepository storeSeatRepository) {
        this.repository = repository;
        this.storeSeatRepository = storeSeatRepository;
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

        for (int retry = 0; retry < 3; retry++) {
            int current = storeSeat.getInUsingSeat();
            int next = current + delta;

            if (next < 0) throw new IllegalArgumentException("사용중 좌석 수는 0 미만이 될 수 없습니다.");
            if (next > store.getSeatNum()) throw new IllegalArgumentException("사용중 좌석 수가 전체 좌석 수를 초과할 수 없습니다.");

            storeSeat.setInUsingSeat(next);
            try {
                storeSeatRepository.saveAndFlush(storeSeat);
                return store.getSeatNum() - next;
            } catch (org.springframework.dao.OptimisticLockingFailureException e) {
                storeSeat = storeSeatRepository.findByStoreId(storeId).orElseThrow();
            }
        }
        throw new IllegalStateException("좌석 증감 처리 중 충돌이 발생했습니다. 다시 시도해주세요.");
    }

    // ===================== 영업시간 판단 로직 =====================

    /** 현재(Asia/Seoul) 기준 영업중 여부. */
    public boolean isOpenNow(Store store) {
        return isOpenAt(store.getServiceTime(), LocalTime.now(ZoneId.of("Asia/Seoul")));
    }

    /** 현재 상태 라벨 ("영업중"/"영업종료"). */
    public String openStatus(Store store) {
        return isOpenNow(store) ? "영업중" : "영업종료";
    }

    /**
     * 지원 포맷:
     * - "HH:mm~HH:mm"
     * - "HH:mm~HH:mm, HH:mm~HH:mm" (다중 구간)
     * - 심야: 종료 < 시작 (예: "22:00~02:00")
     * - 종료가 "24:00"이면 당일 끝까지 영업 (예: "06:00~24:00")
     */
    boolean isOpenAt(String serviceTime, LocalTime now) {
        if (serviceTime == null || serviceTime.isBlank()) return false;
        List<TimeRange> ranges = parseRanges(serviceTime);
        for (TimeRange r : ranges) {
            if (r.contains(now)) return true;
        }
        return false;
    }

    private List<TimeRange> parseRanges(String serviceTime) {
        String[] parts = serviceTime.split(",");
        List<TimeRange> ranges = new ArrayList<>();
        for (String part : parts) {
            String p = part.trim();
            if (p.isEmpty()) continue;
            String[] se = p.split("~");
            if (se.length != 2) continue;

            String startRaw = se[0].trim();
            String endRaw = se[1].trim();

            LocalTime start = parseTimeSafe(startRaw);     // 시작에 24:00은 허용하지 않음
            boolean endIs24 = "24:00".equals(endRaw);      // 종료가 24:00인지
            LocalTime end = endIs24 ? LocalTime.MIDNIGHT : parseTimeSafe(endRaw);

            if (start != null && (endIs24 || end != null)) {
                ranges.add(new TimeRange(start, end, endIs24));
            }
        }
        return ranges;
    }

    private LocalTime parseTimeSafe(String hhmm) {
        try {
            String[] tokens = hhmm.split(":");
            int h = Integer.parseInt(tokens[0]);
            int m = Integer.parseInt(tokens[1]);
            if (h == 24 && m == 0) return LocalTime.MIDNIGHT; // 24:00 표현 보조
            if (h < 0 || h > 23 || m < 0 || m > 59) return null;
            return LocalTime.of(h, m);
        } catch (Exception e) {
            return null;
        }
    }

    static class TimeRange {
        final LocalTime start;
        final LocalTime end;   // endIs24=true면 자정이지만 "당일 끝" 의미
        final boolean endIs24;

        TimeRange(LocalTime s, LocalTime e, boolean endIs24) {
            this.start = s;
            this.end = e;
            this.endIs24 = endIs24;
        }

        boolean contains(LocalTime now) {
            if (endIs24) {
                // 24:00까지: now >= start 면 영업중
                return !now.isBefore(start);
            }
            if (start.equals(end)) return false;
            if (end.isAfter(start)) {
                return !now.isBefore(start) && now.isBefore(end);
            } else {
                // 심야 구간
                return !now.isBefore(start) || now.isBefore(end);
            }
        }
    }
}
