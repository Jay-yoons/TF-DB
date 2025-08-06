package fog.booking_service.dto;

import fog.booking_service.domain.BookingStateCode;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BookingListReponse {

    private Long bookingNum;
    private LocalDateTime bookingDate;
    private String storeId;
    private BookingStateCode bookingStateCode;
}
