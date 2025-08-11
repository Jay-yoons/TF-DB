package com.example.store.service.service;

import org.springframework.stereotype.Service;
import com.example.store.service.entity.Store;
import com.example.store.service.entity.StoreSeat;
import com.example.store.service.repository.StoreRepository;
import com.example.store.service.repository.StoreSeatRepository;

import java.util.List;

@Service
public class StoreService {
    private final StoreRepository repository;
    private final StoreSeatRepository storeSeatRepository;

    public StoreService(StoreRepository repository, StoreSeatRepository storeSeatRepository) {
        this.repository = repository;
        this.storeSeatRepository = storeSeatRepository;
    }

    public List<Store> getAllStores() {
        return repository.findAll();
    }

    public Store getStore(Long storeId) {
        return repository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));
    }

    public Store saveStore(Store store) {
        return repository.save(store);
    }

    // 실시간 좌석 정보 조회 (Booking Service에서 업데이트)
    public StoreSeat getStoreSeatInfo(Long storeId) {
        return storeSeatRepository.findByStoreId(storeId)
                .orElse(StoreSeat.builder()
                        .storeId(storeId)
                        .inUsingSeat(0)
                        .build());
    }

    public int getAvailableSeats(Long storeId) {
        Store store = getStore(storeId);
        StoreSeat storeSeat = getStoreSeatInfo(storeId);
        return store.getSeatNum() - storeSeat.getInUsingSeat();
    }

    // Booking Service에서 호출할 메서드
    public StoreSeat saveStoreSeat(StoreSeat storeSeat) {
        return storeSeatRepository.save(storeSeat);
    }
}
