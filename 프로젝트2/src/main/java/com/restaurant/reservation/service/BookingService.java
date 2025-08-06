package com.restaurant.reservation.service;

import com.restaurant.reservation.entity.Booking;
import com.restaurant.reservation.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public Booking createBooking(Booking booking) {
        logger.info("예약 생성: userId={}, storeId={}", booking.getUserId(), booking.getStoreId());
        
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());
        
        Booking savedBooking = bookingRepository.save(booking);
        logger.info("예약 생성 완료: bookingNum={}", savedBooking.getBookingNum());
        
        return savedBooking;
    }

    public List<Booking> getUserReservations(String userId) {
        logger.info("사용자 예약 목록 조회: userId={}", userId);
        
        List<Booking> reservations = bookingRepository.findByUserId(userId);
        logger.info("사용자 예약 목록 조회 완료: {}개", reservations.size());
        
        return reservations;
    }

    public boolean cancelReservation(Long bookingNum, String userId) {
        logger.info("예약 취소 요청: bookingNum={}, userId={}", bookingNum, userId);
        
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingNum);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            
            // 예약 소유자 확인
            if (!booking.getUserId().equals(userId)) {
                logger.warn("예약 소유자가 아닌 사용자의 취소 요청: bookingNum={}, userId={}", bookingNum, userId);
                return false;
            }
            
            booking.setBookingStateCode("CANCELLED");
            booking.setUpdatedAt(LocalDateTime.now());
            bookingRepository.save(booking);
            
            logger.info("예약 취소 완료: bookingNum={}", bookingNum);
            return true;
        }
        
        logger.warn("예약을 찾을 수 없음: bookingNum={}", bookingNum);
        return false;
    }

    public Booking getBookingById(Long bookingNum) {
        logger.info("예약 조회: bookingNum={}", bookingNum);
        
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingNum);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            logger.info("예약 조회 완료: bookingNum={}", bookingNum);
            return booking;
        }
        
        logger.warn("예약을 찾을 수 없음: bookingNum={}", bookingNum);
        return null;
    }
} 