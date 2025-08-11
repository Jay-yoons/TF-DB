package fog.booking_service.controller;

import fog.booking_service.dto.BookingListResponse;
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

    /*
    내 예약 목록 조회
    */
/*    @GetMapping("/bookings")
    public String findBookingList(Model model) {
        List<BookingListReponse> bookingList = bookingService.getBookingList();
        model.addAttribute("bookingList", bookingList);
        return "booking-list";
    }*/


    @GetMapping("/bookings")
    public List<BookingListResponse> findBookingList(@RequestParam String userId) {
        return bookingService.getBookingList(userId);
    }
/*
    *//*
    예약 상세 조회
    *//*
    @GetMapping("/bookings/{bookingNum}")
    public String findBooking(@PathVariable Long bookingNum, Model model) {
        try {
            Booking booking = bookingService.getBooking(bookingNum);
            model.addAttribute("booking", booking);
            return "booking-detail";
        } catch (NoSuchElementException e) {
            return "redirect:/bookings";
        }
    }

    *//*
    예약 생성 폼
    *//*
    @GetMapping("/bookings/new")
    public String showBookingForm() {
        return "booking";
    }

    *//*
    예약 생성
    *//*
    @PostMapping("/bookings/new")
    public String booking(@ModelAttribute BookingRequest request, RedirectAttributes attributes) {
        try {
            Booking booking = bookingService.makeBooking(request);
            attributes.addAttribute("bookingNum", booking.getBookingNum());
            return "redirect:/bookings/{bookingNum}";
        } catch (IllegalArgumentException e) {
            attributes.addFlashAttribute("error", "예약 정보를 확인해주세요.");
            return "redirect:/bookings/new";
        }
    }

    *//*
    예약 취소 - 예약이 삭제되지 않고 state만 변경
    *//*
    @PatchMapping("/bookings/{bookingNum}")
    public String cancelBooking(@PathVariable Long bookingNum, RedirectAttributes attributes) {
        try {
            bookingService.cancelBooking(bookingNum);
            attributes.addAttribute("bookingNum", bookingNum);
            return "redirect:/bookings/{bookingNum}";
        } catch (IllegalStateException e) {
            // 이미 취소되었거나 취소할 수 없는 상태일 경우
            attributes.addFlashAttribute("error", "예약을 취소할 수 없습니다.");
            attributes.addAttribute("bookingNum", bookingNum);
            return "redirect:/bookings/{bookingNum}";
        } catch (NoSuchElementException e) {
            // 예약 정보를 찾을 수 없을 경우
            attributes.addFlashAttribute("error", "해당 예약을 찾을 수 없습니다.");
            return "redirect:/bookings";
        }
    }*/
}