package com.example.store.service.repository;

import com.example.store.service.entity.StoreSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * 가게 좌석 엔티티용 JPA 레포지토리.
 * - storeId로 단건 조회 제공
 */
public interface StoreSeatRepository extends JpaRepository<StoreSeat, String> {
    Optional<StoreSeat> findByStoreId(String storeId);
}
