package com.example.store.service.repository;

import com.example.store.service.entity.StoreSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * 가게 좌석 엔티티용 JPA 레포지토리.
 *
 * 설계 메모
 * - 엔티티: StoreSeat (PK: String storeId) — 매장별 좌석 현황 1:1 저장
 * - 주요 컬럼: inUsingSeat(사용 중 좌석 수) 등
 *
 * 동시성/락 가이드
 * - 좌석 증감(예약/해제) 시 동시성 문제가 발생하지 않도록
 *   1) 서비스 레이어에서 낙관적 락 버전 필드(@Version) 사용 또는
 *   2) UPDATE … WHERE … AND version = ? 형태로 조건부 갱신, 혹은
 *   3) DB 레벨의 행 잠금(SELECT FOR UPDATE) 전략을 고려하세요.
 *
 * 조회 가이드
 * - storeId로 단건 조회하여 현재 좌석 수를 반환/갱신하는 용도에 사용
 */
public interface StoreSeatRepository extends JpaRepository<StoreSeat, String> {

    /**
     * 매장 ID로 좌석 엔티티 단건 조회.
     *
     * @param storeId 매장 식별자
     * @return Optional<StoreSeat> (없으면 Optional.empty())
     */
    Optional<StoreSeat> findByStoreId(String storeId);
}
