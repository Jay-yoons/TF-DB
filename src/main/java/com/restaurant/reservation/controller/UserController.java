package com.restaurant.reservation.controller;

import com.restaurant.reservation.dto.SignupRequestDto;
import com.restaurant.reservation.dto.SignupResponseDto;
import com.restaurant.reservation.dto.UserInfoDto;
import com.restaurant.reservation.dto.ReviewDto;
import com.restaurant.reservation.dto.FavoriteStoreDto;
import com.restaurant.reservation.entity.User;
import com.restaurant.reservation.service.UserService;
import com.restaurant.reservation.service.AwsCognitoService;
import com.restaurant.reservation.service.StoreServiceIntegration;
import com.restaurant.reservation.config.AwsCognitoConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * User Service 전용 컨트롤러
 * 
 * 사용자 관련 모든 API를 처리합니다.
 * - 회원가입
 * - 로그인/로그아웃
 * - 사용자 정보 관리
 * - JWT 토큰 관리
 * 
 * @author Team-FOG User Service
 * @version 2.0
 * @since 2024-01-15
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {
    "http://localhost:3000",  // Vue.js 개발 서버
    "http://localhost:8080",  // Vite 개발 서버
    "http://localhost:8081",  // 추가 개발 서버
    "http://localhost:8082"   // User Service 자체
})
public class UserController {
    
    // 로깅을 위한 Logger 인스턴스
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    // 사용자 서비스 의존성 주입
    private final UserService userService;
    private final AwsCognitoService cognitoService;
    private final AwsCognitoConfig cognitoConfig;
    private final StoreServiceIntegration storeServiceIntegration;
    
    /**
     * 생성자 - 의존성 주입
     * @param userService 사용자 서비스
     * @param cognitoService AWS Cognito 서비스
     * @param cognitoConfig AWS Cognito 설정
     * @param storeServiceIntegration Store Service 연동 서비스
     */
    public UserController(UserService userService, AwsCognitoService cognitoService, AwsCognitoConfig cognitoConfig, StoreServiceIntegration storeServiceIntegration) {
        this.userService = userService;
        this.cognitoService = cognitoService;
        this.cognitoConfig = cognitoConfig;
        this.storeServiceIntegration = storeServiceIntegration;
    }
    
    /**
     * 통합 마이페이지 조회
     * 사용자 정보 + 통계 + 최근 활동을 한번에 제공
     * 기존 대시보드 기능을 마이페이지에 통합
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMyPage() {
        try {
            String userId = getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            logger.info("통합 마이페이지 조회 요청: userId={}", userId);
            
            Map<String, Object> myPage = userService.getMyPage(userId);
            
            logger.info("통합 마이페이지 조회 완료: userId={}", userId);
            return ResponseEntity.ok(myPage);
            
        } catch (Exception e) {
            logger.error("통합 마이페이지 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 현재 인증된 사용자의 ID를 가져오는 헬퍼 메서드
     */
    private String getCurrentUserId() {
        try {
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getName())) {
                return authentication.getName();
            }
            
            return null;
        } catch (Exception e) {
            logger.error("현재 사용자 ID 조회 중 오류 발생", e);
            return null;
        }
    }
    
    /**
     * Cognito 로그인 URL 생성
     */
    @GetMapping("/login/url")
    public ResponseEntity<Map<String, Object>> generateLoginUrl() {
        try {
            String state = java.util.UUID.randomUUID().toString();
            String loginUrl = cognitoService.generateLoginUrl(state);
            
            Map<String, Object> response = new HashMap<>();
            response.put("url", loginUrl);  // 프론트엔드에서 기대하는 필드명
            response.put("state", state);
            
            logger.info("Cognito 로그인 URL 생성 완료");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("로그인 URL 생성 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    

    
    /**
     * Cognito 콜백 처리 (인증 코드로 토큰 교환)
     */
    @PostMapping("/login/callback")
    public ResponseEntity<Map<String, Object>> handleCallback(@RequestBody Map<String, String> callbackRequest) {
        try {
            String authorizationCode = callbackRequest.get("code");
            String state = callbackRequest.get("state");
            
            if (authorizationCode == null) {
                logger.warn("인증 코드가 누락되었습니다");
                return ResponseEntity.badRequest().build();
            }
            
            logger.info("Cognito 콜백 처리: code={}, state={}", authorizationCode, state);
            
            // 인증 코드로 토큰 교환
            Map<String, Object> tokenResponse = cognitoService.exchangeCodeForToken(authorizationCode);
            
            // 사용자 정보 추출
            String idToken = (String) tokenResponse.get("id_token");
            Map<String, Object> userInfo = cognitoService.getUserInfoFromIdToken(idToken);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("accessToken", tokenResponse.get("access_token"));
            response.put("idToken", idToken);
            response.put("refreshToken", tokenResponse.get("refresh_token"));
            response.put("tokenType", "Bearer");
            response.put("expiresIn", tokenResponse.get("expires_in"));
            response.put("userInfo", userInfo);
            response.put("message", "Cognito 로그인 성공");
            
            logger.info("Cognito 로그인 완료: userId={}", userInfo.get("sub"));
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Cognito 콜백 처리 중 오류 발생", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        try {
            logger.info("로그아웃 요청");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "로그아웃 성공");

            logger.info("로그아웃 완료");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("로그아웃 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 회원가입 처리
     */
    @PostMapping
    public ResponseEntity<SignupResponseDto> signup(@RequestBody SignupRequestDto signupRequest) {
        try {
            logger.info("회원가입 요청: userName={}", signupRequest.getUserName());
            
            User user = userService.signup(
                signupRequest.getUserId(),
                signupRequest.getUserName(),
                signupRequest.getPhoneNumber(),
                signupRequest.getUserLocation(),
                signupRequest.getPassword()
            );
            
            SignupResponseDto signupResponse = new SignupResponseDto(
                user.getUserId(), user.getUserName(), user.getPhoneNumber(),
                user.getUserLocation(), user.getCreatedAt()
            );
            
            logger.info("회원가입 완료: userId={}", signupResponse.getUserId());
            return ResponseEntity.status(HttpStatus.CREATED).body(signupResponse);
            
        } catch (Exception e) {
            logger.error("회원가입 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 전체 사용자 수 조회
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getUserCount() {
        try {
            long count = userService.getUserCount();
            
            Map<String, Object> response = new HashMap<>();
            response.put("count", count);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("사용자 수 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 사용자 정보 수정
     */
    @PutMapping("/me")
    public ResponseEntity<UserInfoDto> updateMyInfo(@RequestBody Map<String, String> updateRequest) {
        try {
            String userId = getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            logger.info("사용자 정보 수정 요청: userId={}", userId);

            // 사용자 정보 수정 로직 구현
            User updatedUser = userService.updateUserInfo(userId, updateRequest);
            UserInfoDto userInfo = new UserInfoDto(
                updatedUser.getUserId(), updatedUser.getUserName(), updatedUser.getPhoneNumber(),
                updatedUser.getUserLocation(), updatedUser.isActive(), updatedUser.getCreatedAt(), updatedUser.getUpdatedAt()
            );

            logger.info("사용자 정보 수정 완료: userId={}", userId);
            return ResponseEntity.ok(userInfo);

        } catch (Exception e) {
            logger.error("사용자 정보 수정 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 서비스 헬스체크
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "user-service");
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 더미 데이터 생성 (개발용)
     * 실제 배포환경에서는 비활성화됨
     */
    @PostMapping("/dummy/data")
    public ResponseEntity<Map<String, Object>> createDummyData() {
        // 실제 배포환경에서는 더미 데이터 생성 비활성화
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "더미 데이터 생성은 개발 환경에서만 사용 가능합니다.");
        
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 마이페이지 - 내가 작성한 리뷰 목록 조회
     */
    @GetMapping("/me/reviews")
    public ResponseEntity<List<ReviewDto>> getMyReviews() {
        try {
            String userId = getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            logger.info("내 리뷰 목록 조회 요청: userId={}", userId);

            if (cognitoConfig.isDummyMode()) {
                // 더미 모드: 더미 리뷰 데이터 반환
                List<ReviewDto> reviews = getDummyReviews(userId);
                logger.info("더미 모드 내 리뷰 목록 조회 완료: userId={}, count={}", userId, reviews.size());
                return ResponseEntity.ok(reviews);
            }

            // 실제 Store Service에서 리뷰 데이터를 가져오기
            List<ReviewDto> reviews = storeServiceIntegration.getUserReviews(userId);

            logger.info("내 리뷰 목록 조회 완료: userId={}, count={}", userId, reviews.size());
            return ResponseEntity.ok(reviews);

        } catch (Exception e) {
            logger.error("내 리뷰 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 가게의 리뷰 페이지로 이동하기 위한 정보 조회
     */
    @GetMapping("/me/reviews/{reviewId}/store-info")
    public ResponseEntity<Map<String, Object>> getStoreInfoForReview(@PathVariable Long reviewId) {
        try {
            String userId = getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            logger.info("리뷰 관련 가게 정보 조회 요청: userId={}, reviewId={}", userId, reviewId);

            if (cognitoConfig.isDummyMode()) {
                // 더미 모드: 더미 가게 정보 반환
                Map<String, Object> storeInfo = getDummyStoreInfo(reviewId);
                logger.info("더미 모드 리뷰 관련 가게 정보 조회 완료: reviewId={}", reviewId);
                return ResponseEntity.ok(storeInfo);
            }

            // 실제 Store Service에서 가게 정보를 가져오기
            Map<String, Object> storeInfo = storeServiceIntegration.getStoreInfoForReview(reviewId);

            logger.info("리뷰 관련 가게 정보 조회 완료: reviewId={}", reviewId);
            return ResponseEntity.ok(storeInfo);

        } catch (Exception e) {
            logger.error("리뷰 관련 가게 정보 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 더미 리뷰 데이터 생성 (실제로는 Store Service에서 가져와야 함)
     */
    private List<ReviewDto> getDummyReviews(String userId) {
        List<ReviewDto> reviews = new ArrayList<>();
        
        reviews.add(new ReviewDto(
            1L, "store001", "맛있는 한식당", userId, "사용자1",
            "정말 맛있었어요! 다음에 또 방문하고 싶습니다.", 5,
            java.time.LocalDateTime.now().minusDays(2), java.time.LocalDateTime.now().minusDays(2)
        ));
        
        reviews.add(new ReviewDto(
            2L, "store002", "신선한 중식당", userId, "사용자1",
            "음식이 신선하고 맛있었습니다. 서비스도 좋았어요.", 4,
            java.time.LocalDateTime.now().minusDays(5), java.time.LocalDateTime.now().minusDays(5)
        ));
        
        reviews.add(new ReviewDto(
            3L, "store003", "분위기 좋은 카페", userId, "사용자1",
            "분위기가 정말 좋고 커피도 맛있었습니다.", 5,
            java.time.LocalDateTime.now().minusDays(10), java.time.LocalDateTime.now().minusDays(10)
        ));
        
        return reviews;
    }

    /**
     * 더미 가게 정보 생성 (실제로는 Store Service에서 가져와야 함)
     */
    private Map<String, Object> getDummyStoreInfo(Long reviewId) {
        Map<String, Object> storeInfo = new HashMap<>();
        
        // reviewId에 따라 다른 가게 정보 반환
        if (reviewId == 1L) {
            storeInfo.put("storeId", "store001");
            storeInfo.put("storeName", "맛있는 한식당");
            storeInfo.put("storeUrl", "/stores/store001/reviews");
        } else if (reviewId == 2L) {
            storeInfo.put("storeId", "store002");
            storeInfo.put("storeName", "신선한 중식당");
            storeInfo.put("storeUrl", "/stores/store002/reviews");
        } else if (reviewId == 3L) {
            storeInfo.put("storeId", "store003");
            storeInfo.put("storeName", "분위기 좋은 카페");
            storeInfo.put("storeUrl", "/stores/store003/reviews");
        } else {
            storeInfo.put("storeId", "unknown");
            storeInfo.put("storeName", "알 수 없는 가게");
            storeInfo.put("storeUrl", "/stores");
        }
        
        return storeInfo;
    }
    
    /**
     * 더미 즐겨찾기 가게 데이터 생성 (실제로는 데이터베이스에서 가져와야 함)
     */
    private List<FavoriteStoreDto> getDummyFavoriteStores(String userId) {
        List<FavoriteStoreDto> favoriteStores = new ArrayList<>();
        
        favoriteStores.add(new FavoriteStoreDto(
            1L, "store001", "맛있는 한식당", userId,
            java.time.LocalDateTime.now().minusDays(5)
        ));
        
        favoriteStores.add(new FavoriteStoreDto(
            2L, "store002", "신선한 중식당", userId,
            java.time.LocalDateTime.now().minusDays(3)
        ));
        
        favoriteStores.add(new FavoriteStoreDto(
            3L, "store003", "분위기 좋은 카페", userId,
            java.time.LocalDateTime.now().minusDays(1)
        ));
        
        return favoriteStores;
    }

    // =============================================================================
    // 즐겨찾기 가게 관련 API
    // =============================================================================

    /**
     * 마이페이지 - 내 즐겨찾기 가게 목록 조회
     */
    @GetMapping("/me/favorites")
    public ResponseEntity<List<FavoriteStoreDto>> getMyFavorites() {
        try {
            String userId = getCurrentUserId();
            logger.info("내 즐겨찾기 가게 목록 조회 요청: userId={}", userId);

            List<FavoriteStoreDto> favorites = userService.getFavoriteStores(userId);
            
            logger.info("내 즐겨찾기 가게 목록 조회 완료: userId={}, count={}", userId, favorites.size());
            return ResponseEntity.ok(favorites);

        } catch (Exception e) {
            logger.error("내 즐겨찾기 가게 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 뷰를 사용한 내 즐겨찾기 가게 상세 정보 조회 (개선된 방식)
     * DB 담당자와 협의 후 사용
     */
    @GetMapping("/me/favorites/details")
    public ResponseEntity<List<Map<String, Object>>> getMyFavoritesWithDetails() {
        try {
            String userId = getCurrentUserId();
            logger.info("뷰를 사용한 내 즐겨찾기 가게 상세 정보 조회 요청: userId={}", userId);

            List<Map<String, Object>> favorites = userService.getFavoriteStoresWithDetails(userId);
            
            logger.info("뷰를 사용한 내 즐겨찾기 가게 상세 정보 조회 완료: userId={}, count={}", userId, favorites.size());
            return ResponseEntity.ok(favorites);

        } catch (Exception e) {
            logger.error("뷰를 사용한 내 즐겨찾기 가게 상세 정보 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 즐겨찾기 가게 추가
     */
    @PostMapping("/me/favorites")
    public ResponseEntity<FavoriteStoreDto> addFavoriteStore(@RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String storeId = request.get("storeId");
            if (storeId == null || storeId.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            logger.info("즐겨찾기 가게 추가 요청: userId={}, storeId={}", userId, storeId);

            if (cognitoConfig.isDummyMode()) {
                // 더미 모드: 더미 즐겨찾기 가게 생성
                FavoriteStoreDto dummyFavoriteStore = new FavoriteStoreDto(
                    999L, storeId, "더미 가게", userId, java.time.LocalDateTime.now()
                );
                logger.info("더미 모드 즐겨찾기 가게 추가 완료: userId={}, storeId={}", userId, storeId);
                return ResponseEntity.status(HttpStatus.CREATED).body(dummyFavoriteStore);
            }

            FavoriteStoreDto favoriteStore = userService.addFavoriteStore(userId, storeId);

            logger.info("즐겨찾기 가게 추가 완료: userId={}, storeId={}", userId, storeId);
            return ResponseEntity.status(HttpStatus.CREATED).body(favoriteStore);

        } catch (Exception e) {
            logger.error("즐겨찾기 가게 추가 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 즐겨찾기 가게 삭제
     */
    @DeleteMapping("/me/favorites/{storeId}")
    public ResponseEntity<Map<String, Object>> removeFavoriteStore(@PathVariable String storeId) {
        try {
            String userId = getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            logger.info("즐겨찾기 가게 삭제 요청: userId={}, storeId={}", userId, storeId);

            userService.removeFavoriteStore(userId, storeId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "즐겨찾기 가게가 삭제되었습니다.");

            logger.info("즐겨찾기 가게 삭제 완료: userId={}, storeId={}", userId, storeId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("즐겨찾기 가게 삭제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 가게의 즐겨찾기 상태 확인
     */
    @GetMapping("/me/favorites/{storeId}/check")
    public ResponseEntity<Map<String, Object>> checkFavoriteStore(@PathVariable String storeId) {
        try {
            String userId = getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            logger.info("즐겨찾기 상태 확인 요청: userId={}, storeId={}", userId, storeId);

            if (cognitoConfig.isDummyMode()) {
                // 더미 모드: 더미 즐겨찾기 상태 반환
                boolean isFavorite = storeId.equals("store001") || storeId.equals("store002") || storeId.equals("store003");
                Map<String, Object> response = new HashMap<>();
                response.put("isFavorite", isFavorite);
                response.put("storeId", storeId);
                logger.info("더미 모드 즐겨찾기 상태 확인 완료: userId={}, storeId={}, isFavorite={}", userId, storeId, isFavorite);
                return ResponseEntity.ok(response);
            }

            boolean isFavorite = userService.isFavoriteStore(userId, storeId);

            Map<String, Object> response = new HashMap<>();
            response.put("isFavorite", isFavorite);
            response.put("storeId", storeId);

            logger.info("즐겨찾기 상태 확인 완료: userId={}, storeId={}, isFavorite={}", userId, storeId, isFavorite);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("즐겨찾기 상태 확인 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 내 즐겨찾기 가게 개수 조회 (기존 API - 통합 마이페이지로 대체됨)
     * @deprecated 통합 마이페이지 API 사용 권장
     */
    @GetMapping("/me/favorites/count")
    @Deprecated
    public ResponseEntity<Map<String, Object>> getMyFavoriteStoreCount() {
        try {
            String userId = getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            logger.info("즐겨찾기 가게 개수 조회 요청: userId={}", userId);

            if (cognitoConfig.isDummyMode()) {
                // 더미 모드: 더미 즐겨찾기 개수 반환
                Map<String, Object> response = new HashMap<>();
                response.put("count", 3);
                logger.info("더미 모드 즐겨찾기 가게 개수 조회 완료: userId={}, count=3", userId);
                return ResponseEntity.ok(response);
            }

            long count = userService.getFavoriteStoreCount(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("count", count);

            logger.info("즐겨찾기 가게 개수 조회 완료: userId={}, count={}", userId, count);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("즐겨찾기 가게 개수 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 뷰를 사용한 내 리뷰 상세 정보 조회 (기존 API - 통합 마이페이지로 대체됨)
     * @deprecated 통합 마이페이지 API 사용 권장
     */
    @GetMapping("/me/reviews/details")
    @Deprecated
    public ResponseEntity<List<Map<String, Object>>> getMyReviewsWithDetails() {
        try {
            String userId = getCurrentUserId();
            logger.info("뷰를 사용한 내 리뷰 상세 정보 조회 요청: userId={}", userId);

            List<Map<String, Object>> reviews = userService.getUserReviewsWithDetails(userId);
            
            logger.info("뷰를 사용한 내 리뷰 상세 정보 조회 완료: userId={}, count={}", userId, reviews.size());
            return ResponseEntity.ok(reviews);

        } catch (Exception e) {
            logger.error("뷰를 사용한 내 리뷰 상세 정보 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 뷰를 사용한 내 예약 현황 조회 (기존 API - 통합 마이페이지로 대체됨)
     * @deprecated 통합 마이페이지 API 사용 권장
     */
    @GetMapping("/me/bookings/details")
    @Deprecated
    public ResponseEntity<List<Map<String, Object>>> getMyBookingsWithDetails() {
        try {
            String userId = getCurrentUserId();
            logger.info("뷰를 사용한 내 예약 현황 조회 요청: userId={}", userId);

            List<Map<String, Object>> bookings = userService.getUserBookingsWithDetails(userId);
            
            logger.info("뷰를 사용한 내 예약 현황 조회 완료: userId={}, count={}", userId, bookings.size());
            return ResponseEntity.ok(bookings);

        } catch (Exception e) {
            logger.error("뷰를 사용한 내 예약 현황 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 뷰를 사용한 내 대시보드 통계 조회 (기존 API - 통합 마이페이지로 대체됨)
     * @deprecated 통합 마이페이지 API 사용 권장
     */
    @GetMapping("/me/dashboard/stats")
    @Deprecated
    public ResponseEntity<Map<String, Object>> getMyDashboardStats() {
        try {
            String userId = getCurrentUserId();
            logger.info("뷰를 사용한 내 대시보드 통계 조회 요청: userId={}", userId);

            Map<String, Object> dashboardStats = userService.getUserDashboardStats(userId);
            
            logger.info("뷰를 사용한 내 대시보드 통계 조회 완료: userId={}", userId);
            return ResponseEntity.ok(dashboardStats);

        } catch (Exception e) {
            logger.error("뷰를 사용한 내 대시보드 통계 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
