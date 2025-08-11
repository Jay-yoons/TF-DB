package fog.booking_service.servivce;

import fog.booking_service.domain.Booking;
import fog.booking_service.domain.BookingStateCode;
import fog.booking_service.dto.BookingListReponse;
import fog.booking_service.dto.BookingRequest;
import fog.booking_service.repositoroy.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;

    /*
    예약 리스트 조회
     */
    public List<BookingListReponse> getBookingList(String userId) {
        log.info("예약 리스트 조회");
        List<Booking> bookings = bookingRepository.findAllByUserId(userId);
        for (Booking booking : bookings) {
            log.info("bookingNum={}", booking.getBookingNum());
        }
        return bookings.stream()
                .map(b -> BookingListReponse.builder()
                        .bookingNum(b.getBookingNum())
                        .bookingDate(b.getBookingDate())
                        .storeId(b.getStoreId())
                        .bookingStateCode(b.getBookingStateCode())
                        .build())
                .collect(Collectors.toList());
    }

    /*
    예약 상세 조회
     */
    public Booking getBooking(Long bookingNum) {
        log.info("예약 상세 조회");
        return bookingRepository.findByBookingNum(bookingNum);
    }

    /*
    예약 생성
     */
    public Booking makeBooking(BookingRequest request) {

        log.info("예약 생성");
        Booking booking = new Booking(request.getBookingDate(), request.getUserId(), request.getStoreId(), request.getCount());
        return bookingRepository.save(booking);
    }

    /*
    예약 취소
     */
    public void cancelBooking(Long bookingNum) {
        log.info("예약 취소");
        Booking booking = bookingRepository.findByBookingNum(bookingNum);
        booking.setBookingStateCode(BookingStateCode.CANCALLED);
    }
}
