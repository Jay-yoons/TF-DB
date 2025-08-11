package com.restaurant.reservation.controller;

import com.restaurant.reservation.entity.User;
import com.restaurant.reservation.service.UserService;
import com.restaurant.reservation.service.JwtService;
import com.restaurant.reservation.config.JwtConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * MSA 환경을 고려한 JWT 기반 로그인 컨트롤러
 * 다른 마이크로서비스와의 토큰 공유를 지원합니다.
 */
@RestController
@RequestMapping("/login")
@CrossOrigin(origins = "*")
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    private final UserService userService;
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;

    public LoginController(UserService userService, JwtService jwtService, JwtConfig jwtConfig) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.jwtConfig = jwtConfig;
    }

    /**
     * JWT 기반 로그인 처리 (MSA 환경용)
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String userId = loginRequest.get("userId");
            String password = loginRequest.get("password");

            logger.info("JWT 로그인 요청: userId={}", userId);

            if (userId == null || password == null) {
                logger.warn("로그인 정보가 누락되었습니다");
                return ResponseEntity.badRequest().build();
            }

            User user = userService.login(userId, password);

            // JWT 토큰 생성 (MSA 환경용)
            String accessToken = jwtService.generateToken(user.getUserId(), user.getUserName(), jwtConfig.getIssuer());
            String refreshToken = jwtService.generateRefreshToken(user.getUserId(), jwtConfig.getIssuer());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", user.getUserId());
            response.put("userName", user.getUserName());
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("tokenType", "Bearer");
            response.put("expiresIn", jwtConfig.getExpiration());
            response.put("message", "JWT 로그인 성공");

            logger.info("JWT 로그인 완료: userId={}", user.getUserId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("JWT 로그인 중 오류 발생", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * JWT 토큰 갱신 (MSA 환경용)
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody Map<String, String> refreshRequest) {
        try {
            String refreshToken = refreshRequest.get("refreshToken");

            logger.info("토큰 갱신 요청");

            if (refreshToken == null) {
                logger.warn("리프레시 토큰이 누락되었습니다");
                return ResponseEntity.badRequest().build();
            }

            // 리프레시 토큰 검증
            if (!jwtService.validateToken(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
                logger.warn("유효하지 않은 리프레시 토큰");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String userId = jwtService.getUserIdFromToken(refreshToken);
            String serviceName = jwtService.getServiceNameFromToken(refreshToken);

            // 새로운 액세스 토큰 생성
            String newAccessToken = jwtService.generateToken(userId, "", serviceName);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("accessToken", newAccessToken);
            response.put("tokenType", "Bearer");
            response.put("expiresIn", jwtConfig.getExpiration());
            response.put("message", "토큰 갱신 성공");

            logger.info("토큰 갱신 완료: userId={}", userId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("토큰 갱신 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * JWT 로그아웃 처리 (MSA 환경용)
     * 실제로는 클라이언트에서 토큰을 삭제하는 방식으로 처리
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestBody(required = false) Map<String, String> logoutRequest) {
        try {
            logger.info("JWT 로그아웃 요청");

            // MSA 환경에서는 토큰 블랙리스트에 추가하는 로직을 구현할 수 있음
            // 현재는 단순히 성공 응답만 반환

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "JWT 로그아웃 성공");

            logger.info("JWT 로그아웃 완료");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("JWT 로그아웃 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * JWT 토큰 상태 확인 (MSA 환경용)
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> checkTokenStatus(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Map<String, Object> response = new HashMap<>();

            if (authHeader != null && authHeader.startsWith(jwtConfig.getTokenPrefix())) {
                String token = authHeader.substring(jwtConfig.getTokenPrefix().length());
                
                if (jwtService.validateToken(token)) {
                    String userId = jwtService.getUserIdFromToken(token);
                    String userName = jwtService.getUserNameFromToken(token);
                    String serviceName = jwtService.getServiceNameFromToken(token);
                    long remainingTime = jwtService.getTokenRemainingTime(token);

                    response.put("valid", true);
                    response.put("userId", userId);
                    response.put("userName", userName);
                    response.put("serviceName", serviceName);
                    response.put("remainingTime", remainingTime);
                } else {
                    response.put("valid", false);
                    response.put("message", "유효하지 않은 토큰");
                }
            } else {
                response.put("valid", false);
                response.put("message", "토큰이 없습니다");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("토큰 상태 확인 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * MSA 서비스 간 토큰 검증 (다른 서비스에서 호출)
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> tokenRequest) {
        try {
            String token = tokenRequest.get("token");
            String requestingService = tokenRequest.get("serviceName");

            logger.info("MSA 토큰 검증 요청: serviceName={}", requestingService);

            Map<String, Object> response = new HashMap<>();

            if (token != null && jwtService.validateToken(token)) {
                String userId = jwtService.getUserIdFromToken(token);
                String tokenServiceName = jwtService.getServiceNameFromToken(token);

                response.put("valid", true);
                response.put("userId", userId);
                response.put("tokenServiceName", tokenServiceName);
                response.put("message", "토큰 검증 성공");

                logger.info("MSA 토큰 검증 성공: userId={}, serviceName={}", userId, tokenServiceName);
            } else {
                response.put("valid", false);
                response.put("message", "유효하지 않은 토큰");
                logger.warn("MSA 토큰 검증 실패: serviceName={}", requestingService);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("MSA 토큰 검증 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
