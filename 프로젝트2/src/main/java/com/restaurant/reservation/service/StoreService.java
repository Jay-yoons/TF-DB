package com.restaurant.reservation.service;

import com.restaurant.reservation.dto.StoreDto;
import com.restaurant.reservation.entity.Store;
import com.restaurant.reservation.repository.StoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StoreService {

    private static final Logger logger = LoggerFactory.getLogger(StoreService.class);

    private final StoreRepository storeRepository;

    public StoreService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public List<StoreDto> getAllStores() {
        logger.info("모든 가게 목록 조회");
        
        List<Store> stores = storeRepository.findAll();
        List<StoreDto> storeDtos = stores.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        logger.info("모든 가게 목록 조회 완료: {}개", storeDtos.size());
        return storeDtos;
    }

    public StoreDto getStoreById(String storeId) {
        logger.info("가게 조회: storeId={}", storeId);
        
        Optional<Store> storeOpt = storeRepository.findById(storeId);
        if (storeOpt.isPresent()) {
            Store store = storeOpt.get();
            logger.info("가게 조회 완료: storeId={}", storeId);
            return convertToDto(store);
        }
        
        logger.warn("가게를 찾을 수 없음: storeId={}", storeId);
        return null;
    }

    public List<StoreDto> searchStoresByName(String storeName) {
        logger.info("가게명으로 검색: storeName={}", storeName);
        
        List<Store> stores = storeRepository.findByStoreNameContaining(storeName);
        List<StoreDto> storeDtos = stores.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        logger.info("가게명 검색 완료: {}개", storeDtos.size());
        return storeDtos;
    }

    public List<StoreDto> searchStoresByLocation(String location) {
        logger.info("위치로 검색: location={}", location);
        
        List<Store> stores = storeRepository.findByStoreLocationContaining(location);
        List<StoreDto> storeDtos = stores.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        logger.info("위치 검색 완료: {}개", storeDtos.size());
        return storeDtos;
    }

    private StoreDto convertToDto(Store store) {
        StoreDto dto = new StoreDto();
        dto.setStoreId(store.getStoreId());
        dto.setStoreName(store.getStoreName());
        dto.setStoreLocation(store.getStoreLocation());
        dto.setSeatNum(store.getSeatNum());
        dto.setCategoryCode(store.getCategoryCode());
        dto.setServiceTime(store.getServiceTime());
        dto.setCreatedAt(store.getCreatedAt());
        dto.setUpdatedAt(store.getUpdatedAt());
        return dto;
    }
} 