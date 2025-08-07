package com.restaurant.reservation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

/**
 * Review Service와의 연동을 위한 컨트롤러
 * 
 * 실제 ECS 환경에서는 Review Service로 요청을 전달하고,
 * 현재는 User Service만 실행 중이므로 더미 데이터를 반환합니다.
 * 
 * @author User Service Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Value("${review.service.url:http://review-service:8080}")
    private String reviewServiceUrl;

    /**
     * 특정 가게의 리뷰 목록을 조회합니다.
     * 
     * @param storeId 가게 ID
     * @return 리뷰 목록 (JSON)
     */
    @GetMapping("/{storeId}")
    public ResponseEntity<List<Map<String, Object>>> getReviewsByStore(@PathVariable String storeId) {
        try {
            // 실제 ECS 환경에서는 Review Service로 요청 전달
            // String url = reviewServiceUrl + "/api/reviews/store/" + storeId;
            // ResponseEntity<List<Map<String, Object>>> response = restTemplate.getForEntity(url, List.class);
            
            // 현재는 User Service만 실행 중이므로 더미 데이터 반환
            List<Map<String, Object>> dummyReviews = getDummyReviews(storeId);
            return ResponseEntity.ok(dummyReviews);
            
        } catch (Exception e) {
            // Review Service 연결 실패 시 더미 데이터 반환
            List<Map<String, Object>> dummyReviews = getDummyReviews(storeId);
            return ResponseEntity.ok(dummyReviews);
        }
    }

    /**
     * 현재 로그인한 사용자의 리뷰 목록을 조회합니다.
     * 
     * @return 사용자의 리뷰 목록 (JSON)
     */
    @GetMapping("/my")
    public ResponseEntity<List<Map<String, Object>>> getMyReviews() {
        try {
            // 실제 ECS 환경에서는 Review Service로 요청 전달
            // String url = reviewServiceUrl + "/api/reviews/my";
            // ResponseEntity<List<Map<String, Object>>> response = restTemplate.getForEntity(url, List.class);
            
            // 현재는 User Service만 실행 중이므로 더미 데이터 반환
            List<Map<String, Object>> dummyReviews = getDummyMyReviews();
            return ResponseEntity.ok(dummyReviews);
            
        } catch (Exception e) {
            // Review Service 연결 실패 시 더미 데이터 반환
            List<Map<String, Object>> dummyReviews = getDummyMyReviews();
            return ResponseEntity.ok(dummyReviews);
        }
    }

    /**
     * 특정 가게의 더미 리뷰 데이터를 반환합니다.
     * 
     * @param storeId 가게 ID
     * @return 더미 리뷰 목록
     */
    private List<Map<String, Object>> getDummyReviews(String storeId) {
        Map<String, List<Map<String, Object>>> dummyReviews = new HashMap<>();
        
        // STORE001의 더미 리뷰
        dummyReviews.put("STORE001", Arrays.asList(
            Map.of(
                "reviewId", "REV001",
                "userId", "user001",
                "userName", "김철수",
                "comment", "정말 맛있어요!",
                "ratio", 5,
                "createdAt", "2024-01-10"
            ),
            Map.of(
                "reviewId", "REV002", 
                "userId", "user002",
                "userName", "이영희",
                "comment", "친절하고 분위기 좋아요.",
                "ratio", 4,
                "createdAt", "2024-01-09"
            )
        ));
        
        // STORE002의 더미 리뷰
        dummyReviews.put("STORE002", Arrays.asList(
            Map.of(
                "reviewId", "REV003",
                "userId", "user003", 
                "userName", "박민수",
                "comment", "파스타가 최고!",
                "ratio", 5,
                "createdAt", "2024-01-11"
            )
        ));
        
        // STORE003의 더미 리뷰
        dummyReviews.put("STORE003", Arrays.asList(
            Map.of(
                "reviewId", "REV004",
                "userId", "user004",
                "userName", "최지영", 
                "comment", "신선한 재료, 훌륭한 서비스.",
                "ratio", 5,
                "createdAt", "2024-01-12"
            )
        ));
        
        return dummyReviews.getOrDefault(storeId, Arrays.asList());
    }

    /**
     * 현재 사용자의 더미 리뷰 데이터를 반환합니다.
     * 
     * @return 더미 내 리뷰 목록
     */
    private List<Map<String, Object>> getDummyMyReviews() {
        return Arrays.asList(
            Map.of(
                "reviewId", "REV001",
                "storeId", "STORE001",
                "storeName", "맛있는 한정식",
                "comment", "정말 맛있었어요! 다음에 또 방문하고 싶습니다.",
                "ratio", 5,
                "createdAt", "2024-01-15"
            ),
            Map.of(
                "reviewId", "REV002",
                "storeId", "STORE003", 
                "storeName", "프리미엄 스시",
                "comment", "신선한 재료와 정성스러운 손길이 느껴졌습니다.",
                "ratio", 4,
                "createdAt", "2024-01-10"
            )
        );
    }
}
