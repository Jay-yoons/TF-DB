package com.example.store.service.repository;

import com.example.store.service.entity.StoreNameMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.store.service.entity.Store;
import java.util.List;

/**
 * 스토어 엔티티용 JPA 레포지토리.
 *
 * 설계 메모
 * - 엔티티: Store (PK: String storeId)
 * - 대표 컬럼: storeName, categoryCode, storeLocation, seatNum, serviceTime, latitude/longitude
 * - 인덱스 권장: categoryCode, storeLocation(전방/후방 검색 시 별도 전략 고려)
 *
 * 트랜잭션/성능 가이드
 * - 목록은 필터(카테고리 등) + 페이징/정렬(Sort) 조합을 권장
 * - 좌표 기반 조회가 필요하면 DB 공간 인덱스/외부 검색(예: 엘라스틱) 검토
 */
public interface StoreRepository extends JpaRepository<Store, String> {

	/**
	 * 카테고리 코드로 매장 목록 조회.
	 * - 결과 수가 많을 수 있으므로 실제 화면에서는 Pageable/Sort 확장을 권장
	 *
	 * @param categoryCode 카테고리 코드
	 * @return 해당 카테고리의 매장 목록
	 */
	List<Store> findByCategoryCode(Integer categoryCode);

	StoreNameMapping findByStoreId(String storeId);

}
