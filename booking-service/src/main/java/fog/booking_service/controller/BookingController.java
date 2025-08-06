package fog.booking_service.controller;

import fog.booking_service.domain.Booking;
import fog.booking_service.dto.BookingListReponse;
import fog.booking_service.servivce.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /*
    내 예약 목록 조회
     */
    @GetMapping("/bookings")
    public String findBookingList(Model model) {
//        List<BookingListReponse> bookingList = bookingService.getBookingList();

//        model.addAttribute("bookingList", bookingList);

        return "booking-list";
    }

    /*
    예약 상세 조회
     */
    @GetMapping("/bookings/{bookingNum}")
    public String findBooking(@PathVariable Long bookingNum, Model model) {

        Booking booking = bookingService.getBooking(bookingNum);
        model.addAttribute("booking", booking);

        return "booking-detail";
    }

    /*
    예약 생성
     */
    @PostMapping("/bookings")
    public String booking(Model model) {
        return "booking";
    }

    /*
    예약 취소 - 예약이 삭제되지 않고 state만 변경
     */
    @PatchMapping("/bookings/{bookingNum}")
    public String cancelBooking(Model model) {
        return "redirect:/booking-list";
    }
}
