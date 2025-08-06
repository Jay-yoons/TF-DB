package fog.booking_service.servivce;

import fog.booking_service.domain.Booking;
import fog.booking_service.dto.BookingListReponse;
import fog.booking_service.repositoroy.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;

    public List<Booking> getBookingList(String userId) {
        return bookingRepository.findByUserId(userId);
    }

    public Booking getBooking(Long bookingNum) {
        return bookingRepository.findByBookingNum(bookingNum);
    }
}
