package fog.booking_service.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "BOOKING")
@NoArgsConstructor
@Getter
@Setter
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BOOKING_NUM")
    private Long bookingNum;

    @Column(name = "BOOKING_DATE", nullable = false)
    private LocalDateTime bookingDate;

    @Column(name = "USER_ID", nullable = false, length = 15)
    private String userId;

    @Column(name = "STORE_ID", nullable = false, length = 20)
    private String storeId;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "BOOKING_STATE_CODE", nullable = false, columnDefinition = "INT DEFAULT 0")
    private BookingStateCode bookingStateCode;

    @Column(name = "COUNT", nullable = false)
    private int count;

    public Booking(LocalDateTime bookingDate, String userId, String storeId, int count) {
        this.bookingDate = bookingDate;
        this.userId = userId;
        this.storeId = storeId;
        this.count = count;
    }
}