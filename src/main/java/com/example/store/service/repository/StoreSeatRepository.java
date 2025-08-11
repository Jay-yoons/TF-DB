package com.example.store.service.repository;

import com.example.store.service.entity.StoreSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StoreSeatRepository extends JpaRepository<StoreSeat, Long> {
    Optional<StoreSeat> findByStoreId(Long storeId);
}
