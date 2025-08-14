package com.restaurant.reservation.controller;

import com.restaurant.reservation.config.AwsCognitoConfig;
import com.restaurant.reservation.dto.FavoriteStoreDto;
import com.restaurant.reservation.dto.ReviewDto;
import com.restaurant.reservation.dto.SignupRequestDto;
import com.restaurant.reservation.dto.SignupResponseDto;
import com.restaurant.reservation.dto.UserInfoDto;
import com.restaurant.reservation.entity.User;
import com.restaurant.reservation.service.AwsCognitoService;
import com.restaurant.reservation.service.StoreServiceIntegration;
import com.restaurant.reservation.service.UserService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") // CORS 설정 - 모든 도메인에서 접근 허용
@AllArgsConstructor
public class UserController {

    // 로깅을 위한 Logger 인스턴스
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    // 사용자 서비스 의존성 주입
    private final UserService userService;
    private final AwsCognitoService cognitoService;
    private final StoreServiceIntegration storeServiceIntegration;

    /**
     * 내 정보 조회 (JWT 기반)
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfoDto> getMyInfo() {
        try {
            logger.info("내 정보 조회 요청");

            // Spring Security 컨텍스트에서 현재 인증된 사용자 정보 가져오기
            String userId = getCurrentUserId();
            if (userId == null) {
                logger.warn("로그인이 필요합니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            User user = userService.getUserInfo(userId);
            UserInfoDto userInfo = new UserInfoDto(
                    user.getUserId(), user.getUserName(), user.getPhoneNumber(),
                    user.getUserLocation()
            );

            logger.info("내 정보 조회 완료: userId={}", userId);
            return ResponseEntity.ok(userInfo);

        } catch (Exception e) {
            logger.error("내 정보 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 현재 인증된 사용자의 ID를 가져오는 헬퍼 메서드
     */
    private String getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

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
            response.put("loginUrl", loginUrl);
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

            // 1. 인증 코드로 토큰 교환
            Map<String, Object> tokenResponse = cognitoService.exchangeCodeForToken(authorizationCode);
            String idTokenString = (String) tokenResponse.get("id_token");

            // 2. ID 토큰에서 사용자 정보(클레임) 추출
            Map<String, Object> userInfo = cognitoService.getUserInfoFromIdToken(idTokenString);
            String sub = (String) userInfo.get("sub");
            String username = (String) userInfo.get("name");
            String phoneNumber = (String) userInfo.get("phone_number");
            String location = (String) userInfo.get("address");
            // 이메일, 전화번호 등 다른 클레임도 필요에 따라 가져올 수 있습니다.

            // 3. 추출한 sub ID로 DB에 사용자가 있는지 확인
            if (!userService.isUserExists(sub)) {
                logger.info("첫 로그인: DB에 사용자 정보 생성. sub={}", sub);

                // 사용자 정보를 DB에 저장 (추출한 정보와 기본 위치 사용)
                User user = userService.signup(sub, username, phoneNumber, location);
                logger.info("DB에 사용자 정보 저장 완료: userId={}", user.getUserId());
            }

            // 4. 프론트엔드로 토큰 정보 반환
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("accessToken", tokenResponse.get("access_token"));
            response.put("idToken", tokenResponse.get("id_token"));
            response.put("refreshToken", tokenResponse.get("refresh_token"));
            response.put("expiresIn", tokenResponse.get("expires_in"));

            logger.info("Cognito 로그인 및 DB 연동 완료");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Cognito 콜백 처리 중 오류 발생", e);
            // 오류 처리
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
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
    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDto> signup(@RequestBody SignupRequestDto signupRequest) {
        try {
            logger.info("회원가입 요청: userName={}", signupRequest.getUserName());

            // Cognito를 통해 사용자 생성
            // 이 시점에 Cognito는 고유한 'sub' ID를 생성하지만, 이 API에서는 그 값을 얻을 수 없습니다.
            cognitoService.signUp(signupRequest.getUserId(), signupRequest.getPhoneNumber());

            // 회원가입 성공 응답을 반환
            // DB에 저장하는 로직은 삭제합니다.
            SignupResponseDto signupResponse = new SignupResponseDto(
                    signupRequest.getUserId(),
                    signupRequest.getUserName(),
                    signupRequest.getPhoneNumber(),
                    signupRequest.getUserLocation()
            );

            logger.info("회원가입 요청 성공: userId={}", signupResponse.getUserId());
            return ResponseEntity.status(HttpStatus.CREATED).body(signupResponse);

        } catch (Exception e) {
            logger.error("회원가입 중 오류 발생", e);
            // 회원가입 실패 시 Internal Server Error를 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    ///
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
                    updatedUser.getUserId(), updatedUser.getUserName(), updatedUser.getPhoneNumber(), updatedUser.getUserLocation());

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
     * 대시보드 통계 데이터 조회
     * 프론트엔드 HomePage에서 사용
     */
    @GetMapping("/dashboard/counts")
    public ResponseEntity<Map<String, Object>> getDashboardCounts() {
        try {
            logger.info("대시보드 통계 데이터 조회 요청");

            Map<String, Object> response = new HashMap<>();

                // 실제 데이터베이스에서 통계 조회
                long userCount = userService.getUserCount();
                // TODO: Store Service와 Reservation Service에서 데이터 가져오기
                response.put("stores", 0);  // Store Service 연동 필요
                response.put("members", userCount);
                response.put("bookings", 0); // Reservation Service 연동 필요
                logger.info("대시보드 통계 조회 완료: users={}", userCount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("대시보드 통계 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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

            // 실제 Store Service에서 가게 정보를 가져오기
            Map<String, Object> storeInfo = storeServiceIntegration.getStoreInfoForReview(reviewId);

            logger.info("리뷰 관련 가게 정보 조회 완료: reviewId={}", reviewId);
            return ResponseEntity.ok(storeInfo);

        } catch (Exception e) {
            logger.error("리뷰 관련 가게 정보 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =============================================================================
    // 즐겨찾기 가게 관련 API
    // =============================================================================

    /**
     * 마이페이지 - 내 즐겨찾기 가게 목록 조회
     */
    @GetMapping("/me/favorites")
    public ResponseEntity<List<FavoriteStoreDto>> getMyFavoriteStores() {
        try {
            String userId = getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            logger.info("내 즐겨찾기 가게 목록 조회 요청: userId={}", userId);

            List<FavoriteStoreDto> favoriteStores = userService.getFavoriteStores(userId);

            logger.info("내 즐겨찾기 가게 목록 조회 완료: userId={}, count={}", userId, favoriteStores.size());
            return ResponseEntity.ok(favoriteStores);

        } catch (Exception e) {
            logger.error("내 즐겨찾기 가게 목록 조회 중 오류 발생", e);
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

            FavoriteStoreDto favoriteStore = userService.addFavoriteStore(userId, storeId);

            logger.info("즐겨찾기 가게 추가 완료: userId={}, storeId={}", userId, storeId);
            logger.info("null값 확인 - createdAt={}", favoriteStore.getCreatedAt());
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
     * 내 즐겨찾기 가게 개수 조회
     */
    @GetMapping("/me/favorites/count")
    public ResponseEntity<Map<String, Object>> getMyFavoriteStoreCount() {
        try {
            String userId = getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            logger.info("즐겨찾기 가게 개수 조회 요청: userId={}", userId);

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
}