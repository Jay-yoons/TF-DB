package com.restaurant.reservation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @GetMapping("/mypage")
    public String mypage() {
        return "mypage";
    }

    @GetMapping("/restaurants")
    public String restaurants() {
        return "restaurants";
    }

    @GetMapping("/reservations")
    public String reservations() {
        return "reservations";
    }

    @GetMapping("/logout-page")
    public String logoutPage() {
        return "redirect:/";
    }
} 