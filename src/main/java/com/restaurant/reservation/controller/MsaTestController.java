package com.restaurant.reservation.controller;

import com.restaurant.reservation.service.JwtService;
import com.restaurant.reservation.service.MsaIntegrationService;
import com.restaurant.reservation.config.JwtConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * MSA 환경에서 JWT 기반 인증을 테스트하기 위한 컨트롤러
 * 팀원들이 다른 서비스와의 연동을 테스트할 수 있습니다.
 */
@RestController
@RequestMapping("/api/msa-test")
@CrossOrigin(origins = "*")
public class MsaTestController {
    
    private static final Logger logger = LoggerFactory.getLogger(MsaTestController.class);
    
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;
    private final MsaIntegrationService msaIntegrationService;
    
    public MsaTestController(JwtService jwtService, JwtConfig jwtConfig, MsaIntegrationService msaIntegrationService) {
        this.jwtService = jwtService;
        this.jwtConfig = jwtConfig;
        this.msaIntegrationService = msaIntegrationService;
    }
    
    /**
     * JWT 토큰 생성 테스트
     */
    @PostMapping("/generate-token")
    public ResponseEntity<Map<String, Object>> generateTestToken(@RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            String userName = request.get("userName");
            
            if (userId == null || userName == null) {
                return ResponseEntity.badRequest().build();
            }
            
            // JWT 토큰 생성
            String accessToken = jwtService.generateToken(userId, userName, jwtConfig.getIssuer());
            String refreshToken = jwtService.generateRefreshToken(userId, jwtConfig.getIssuer());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("tokenType", "Bearer");
            response.put("expiresIn", jwtConfig.getExpiration());
            response.put("message", "테스트 토큰 생성 성공");
            
            logger.info("테스트 토큰 생성: userId={}", userId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("테스트 토큰 생성 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * JWT 토큰 검증 테스트
     */
    @PostMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateTestToken(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            
            if (token == null) {
                return ResponseEntity.badRequest().build();
            }
            
            Map<String, Object> response = new HashMap<>();
            
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
                response.put("message", "토큰 검증 성공");
                
                logger.info("토큰 검증 성공: userId={}", userId);
            } else {
                response.put("valid", false);
                response.put("message", "토큰 검증 실패");
                logger.warn("토큰 검증 실패");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("토큰 검증 테스트 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * MSA 서비스 간 통신 테스트
     */
    @GetMapping("/test-communication")
    public ResponseEntity<Map<String, Object>> testServiceCommunication() {
        try {
            logger.info("MSA 서비스 간 통신 테스트 요청");
            
            // 메뉴 서비스 호출 테스트
            ResponseEntity<Map<String, Object>> menuResponse = msaIntegrationService.getMenuInfo("STORE001");
            
            // 주문 서비스 호출 테스트
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("bookingId", "TEST_BOOKING_001");
            orderData.put("storeId", "STORE001");
            orderData.put("userId", "testUser");
            orderData.put("bookingDate", "2024-01-20");
            orderData.put("bookingTime", "18:00");
            orderData.put("count", 4);
            
            ResponseEntity<Map<String, Object>> orderResponse = msaIntegrationService.sendReservationToOrderService(orderData);
            
            // 알림 서비스 호출 테스트
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("notificationId", "TEST_NOTIFICATION_001");
            notificationData.put("userId", "testUser");
            notificationData.put("type", "RESERVATION_CONFIRMED");
            notificationData.put("message", "예약이 확정되었습니다.");
            
            ResponseEntity<Map<String, Object>> notificationResponse = msaIntegrationService.sendNotificationToNotificationService(notificationData);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "MSA 서비스 간 통신 테스트 완료");
            result.put("menuService", menuResponse.getBody());
            result.put("orderService", orderResponse.getBody());
            result.put("notificationService", notificationResponse.getBody());
            result.put("httpStatuses", Map.of(
                "menuService", menuResponse.getStatusCode().value(),
                "orderService", orderResponse.getStatusCode().value(),
                "notificationService", notificationResponse.getStatusCode().value()
            ));
            
            logger.info("MSA 서비스 간 통신 테스트 완료");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("MSA 서비스 간 통신 테스트 중 오류 발생", e);
            
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "MSA 서비스 간 통신 테스트 실패: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
        }
    }
    
    /**
     * 허용된 서비스 목록 조회
     */
    @GetMapping("/allowed-services")
    public ResponseEntity<Map<String, Object>> getAllowedServices() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("allowedServices", jwtConfig.getAllowedServices());
            response.put("currentService", jwtConfig.getIssuer());
            response.put("message", "허용된 서비스 목록 조회 성공");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("허용된 서비스 목록 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * JWT 설정 정보 조회
     */
    @GetMapping("/jwt-config")
    public ResponseEntity<Map<String, Object>> getJwtConfig() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("issuer", jwtConfig.getIssuer());
            response.put("expiration", jwtConfig.getExpiration());
            response.put("refreshExpiration", jwtConfig.getRefreshExpiration());
            response.put("headerName", jwtConfig.getHeaderName());
            response.put("tokenPrefix", jwtConfig.getTokenPrefix());
            response.put("message", "JWT 설정 정보 조회 성공");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("JWT 설정 정보 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
