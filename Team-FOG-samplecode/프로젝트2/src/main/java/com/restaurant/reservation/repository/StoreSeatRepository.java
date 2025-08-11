package com.restaurant.reservation.repository;

import com.restaurant.reservation.entity.StoreSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreSeatRepository extends JpaRepository<StoreSeat, String> {
} 