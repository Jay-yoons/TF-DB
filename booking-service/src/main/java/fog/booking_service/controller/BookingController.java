package fog.booking_service.controller;

import fog.booking_service.domain.Booking;
import fog.booking_service.dto.BookingListResponse;
import fog.booking_service.dto.BookingRequest;
import fog.booking_service.dto.BookingResponse;
import fog.booking_service.servivce.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;


    /**
     * 내 예약 목록 조회
     */
    @GetMapping("/bookings")
    public List<BookingListResponse> findBookingList(@RequestParam String userId) {
        return bookingService.getBookingList(userId);
    }

    /**
     * 예약 상세 조회
     */
    @GetMapping("/bookings/{bookingNum}")
    public BookingResponse findBooking(@PathVariable Long bookingNum) {
        return bookingService.getBooking(bookingNum);
    }

    /**
     * 예약 생성
     */
    @PostMapping("/bookings/new")
    public BookingResponse booking(@RequestBody BookingRequest request) {
        log.info("userId={}", request.getUserId());
        log.info("storeId={}", request.getStoreId());
        log.info("bookingDate={}", request.getBookingDate());
        log.info("count={}", request.getCount());
        return bookingService.makeBooking(request);
    }

    /**
     * 예약 취소
     */
    @PatchMapping("/bookings/{bookingNum}")
    public BookingResponse cancelBooking(@PathVariable Long bookingNum) {
        bookingService.cancelBooking(bookingNum);
        return bookingService.getBooking(bookingNum);
    }
}