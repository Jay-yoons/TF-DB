package com.restaurant.reservation.repository;

import com.restaurant.reservation.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(String userId);
    List<Booking> findByStoreId(String storeId);
    List<Booking> findByUserIdAndBookingStateCode(String userId, String bookingStateCode);
} 