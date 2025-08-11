package com.restaurant.reservation.repository;

import com.restaurant.reservation.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, String> {
    List<Store> findByStoreNameContaining(String storeName);
    List<Store> findByStoreLocationContaining(String location);
} 