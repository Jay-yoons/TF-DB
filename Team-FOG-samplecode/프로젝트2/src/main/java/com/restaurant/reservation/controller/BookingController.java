package com.restaurant.reservation.controller;

import com.restaurant.reservation.entity.Booking;
import com.restaurant.reservation.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<Booking> createReservation(@RequestBody Map<String, Object> reservationRequest, HttpSession session) {
        try {
            logger.info("예약 생성 요청: {}", reservationRequest);
            
            String userId = (String) session.getAttribute("userId");
            if (userId == null) {
                logger.warn("로그인되지 않은 사용자의 예약 요청");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            String storeId = (String) reservationRequest.get("storeId");
            String bookingDateStr = (String) reservationRequest.get("bookingDate");
            Integer count = (Integer) reservationRequest.get("count");
            
            if (storeId == null || bookingDateStr == null || count == null) {
                logger.warn("예약 정보 누락");
                return ResponseEntity.badRequest().build();
            }
            
            LocalDate bookingDate = LocalDate.parse(bookingDateStr);
            
            Booking booking = new Booking();
            booking.setUserId(userId);
            booking.setStoreId(storeId);
            booking.setBookingDate(bookingDate);
            booking.setCount(count);
            booking.setBookingStateCode("CONFIRMED");
            
            Booking createdBooking = bookingService.createBooking(booking);
            logger.info("예약 생성 완료: bookingNum={}", createdBooking.getBookingNum());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBooking);
        } catch (Exception e) {
            logger.error("예약 생성 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<Booking>> getUserReservations(HttpSession session) {
        try {
            logger.info("사용자 예약 목록 조회 요청");
            
            String userId = (String) session.getAttribute("userId");
            if (userId == null) {
                logger.warn("로그인되지 않은 사용자의 예약 조회 요청");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            List<Booking> reservations = bookingService.getUserReservations(userId);
            logger.info("사용자 예약 목록 조회 완료: {}개", reservations.size());
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            logger.error("사용자 예약 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/reservations/{bookingNum}")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long bookingNum, HttpSession session) {
        try {
            logger.info("예약 취소 요청: bookingNum={}", bookingNum);
            
            String userId = (String) session.getAttribute("userId");
            if (userId == null) {
                logger.warn("로그인되지 않은 사용자의 예약 취소 요청");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            boolean success = bookingService.cancelReservation(bookingNum, userId);
            if (success) {
                logger.info("예약 취소 완료: bookingNum={}", bookingNum);
                return ResponseEntity.ok().build();
            } else {
                logger.warn("예약 취소 실패: bookingNum={}", bookingNum);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("예약 취소 중 오류 발생: bookingNum={}", bookingNum, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 