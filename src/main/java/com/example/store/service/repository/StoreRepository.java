package com.example.store.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.store.service.entity.Store;

public interface StoreRepository extends JpaRepository<Store, Long> {
}
