// Java
// 파일: src/main/java/com/example/store/service/repository/FavStoreRepository.java
package com.example.store.service.repository;

import com.example.store.service.entity.FavStore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 즐겨찾기 리포지토리(JPA).
 *
 * 설계 메모
 * - 엔티티: FavStore (PK: Long favStoreId)
 * - 유니크 제약: (STORE_ID, USER_ID) 한 사용자 당 하나의 매장에 대해 1개의 즐겨찾기만 허용
 * - 인덱스 권장: USER_ID, STORE_ID 각각에 인덱스를 두면 조회 성능 향상
 * - 비정규화: STORE_NAME 컬럼을 함께 저장하여 목록 표시 시 조인 최소화(정책에 따라 갱신/스냅샷)
 *
 * 트랜잭션/성능 가이드
 * - 다량 조회는 페이징(Pageable) 사용을 권장 (findByUserId(Pageable) 시그니처로 확장 가능)
 * - 중복 여부 체크는 Optional로 받되, 서비스 레이어에서 존재 여부로 분기 처리
 *
 * 사용 예시
 * - 생성 전 중복 검사: findByStoreIdAndUserId(storeId, userId).isPresent()
 * - 목록 화면: findByUserId(userId)
 */
public interface FavStoreRepository extends JpaRepository<FavStore, Long> {

    /**
     * 동일 사용자-가게 조합 존재 여부 조회.
     * - 유니크 제약(STORE_ID, USER_ID) 충돌 방지에 사용
     *
     * @param storeId 매장 식별자
     * @param userId  사용자 식별자
     * @return 존재하면 Optional<FavStore>, 없으면 Optional.empty()
     */
    Optional<FavStore> findByStoreIdAndUserId(String storeId, String userId);

    /**
     * 사용자의 즐겨찾기 전체 조회.
     * - 결과가 많을 수 있으므로 실제 화면에서는 Pageable 확장을 고려
     *
     * @param userId 사용자 식별자
     * @return 즐겨찾기 엔티티 리스트(최신순 정렬이 필요하면 Sort/Specification 사용)
     */
    List<FavStore> findByUserId(String userId);
}
