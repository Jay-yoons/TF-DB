package com.example.store.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.store.service.entity.Store;
import java.util.List;

/**
 * 스토어 엔티티용 JPA 레포지토리.
 */
public interface StoreRepository extends JpaRepository<Store, String> {
	List<Store> findByCategoryCode(Integer categoryCode);
}
