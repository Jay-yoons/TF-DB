package com.example.store.service.controller;

import com.example.store.service.entity.StoreSeat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.store.service.entity.Store;
import com.example.store.service.service.StoreService;

import java.util.List;

@Controller
@RequestMapping("/stores")
public class StoreController {

    private final StoreService service;

    public StoreController(StoreService service) {
        this.service = service;
    }

    // 가게 목록 페이지 (메인)
    @GetMapping
    public String listStores(Model model) {
        List<Store> stores = service.getAllStores();
        model.addAttribute("stores", stores);
        return "store/list"; // src/main/resources/templates/store/list.html
    }

    // 가게 상세 페이지
    @GetMapping("/{storeId}")
    public String storeDetail(@PathVariable Long storeId, Model model) {
        Store store = service.getStore(storeId);
        int availableSeats = service.getAvailableSeats(storeId);
        
        model.addAttribute("store", store);
        model.addAttribute("availableSeats", availableSeats);
        return "store/detail"; // src/main/resources/templates/store/detail.html
    }

    // Booking Service에서 호출할 REST API (좌석 정보 업데이트)
    @PutMapping("/{storeId}/seats")
    @ResponseBody
    public String updateSeatInfo(@PathVariable Long storeId, @RequestParam int inUsingSeat) {
        StoreSeat storeSeat = service.getStoreSeatInfo(storeId);
        storeSeat.setInUsingSeat(inUsingSeat);
        service.saveStoreSeat(storeSeat);
        return "좌석 정보가 업데이트되었습니다.";
    }

    // Booking Service에서 호출할 REST API (여유 좌석 조회)
    @GetMapping("/{storeId}/available-seats")
    @ResponseBody
    public int getAvailableSeats(@PathVariable Long storeId) {
        return service.getAvailableSeats(storeId);
    }
}
