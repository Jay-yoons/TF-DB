// Java
// 파일: src/main/java/com/example/store/service/controller/FavoriteController.java
package com.example.store.service.controller;

import com.example.store.service.entity.FavStore;
import com.example.store.service.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 즐겨찾기 API.
 * - POST /api/favorites?userId=U1&storeId=S1  : 추가
 * - DELETE /api/favorites?userId=U1&storeId=S1: 제거
 * - GET /api/favorites/me?userId=U1           : 내 즐겨찾기 목록
 *
 * [주의] 인증 연동 시 userId는 토큰에서 추출하도록 변경하는 것이 일반적입니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    // [생성] 즐겨찾기 추가
    @PostMapping
    public FavStore add(@RequestParam String userId, @RequestParam String storeId) {
        return favoriteService.addFavorite(userId, storeId);
    }

    // [삭제] 즐겨찾기 제거
    @DeleteMapping
    public void remove(@RequestParam String userId, @RequestParam String storeId) {
        favoriteService.removeFavorite(userId, storeId);
    }

    // [조회] 나의 즐겨찾기 목록
    @GetMapping("/me")
    public List<FavStore> my(@RequestParam String userId) {
        return favoriteService.getMyFavorites(userId);
    }
}
