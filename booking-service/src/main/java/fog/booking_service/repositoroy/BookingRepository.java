package fog.booking_service.repositoroy;

import fog.booking_service.domain.Booking;
import fog.booking_service.dto.BookingListReponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(String userId);

    Booking findByBookingNum(Long bookingNum);
}
