package com.example.store.service.repository;

import com.example.store.service.entity.StoreLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * STORE_LOCATION 엔티티용 JPA 레포지토리.
 */
public interface StoreLocationRepository extends JpaRepository<StoreLocation, String> {

    Optional<StoreLocation> findByStoreId(String storeId);
}
