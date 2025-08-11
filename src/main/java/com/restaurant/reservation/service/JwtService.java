package com.restaurant.reservation.service;

import com.restaurant.reservation.config.JwtConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * MSA 환경을 고려한 JWT 토큰 생성, 검증, 파싱을 담당하는 서비스 클래스
 * 
 * 이 클래스는 다른 마이크로서비스와의 토큰 공유를 지원하며,
 * JWT 토큰의 전체 생명주기를 관리합니다.
 * 
 * 주요 기능:
 * 1. JWT 토큰 생성 (액세스 토큰, 리프레시 토큰)
 * 2. 토큰 검증 및 파싱
 * 3. 토큰에서 사용자 정보 추출
 * 4. MSA 환경에서 서비스 간 토큰 공유 지원
 * 
 * JWT 토큰 구조:
 * - Header: 토큰 타입과 서명 알고리즘 정보
 * - Payload: 사용자 정보, 서비스 정보, 만료 시간 등
 * - Signature: 토큰의 무결성을 보장하는 서명
 * 
 * MSA 환경에서의 토큰 공유:
 * - 모든 서비스가 동일한 시크릿 키를 사용
 * - 토큰에 발급 서비스 정보 포함
 * - 허용된 서비스 목록에서 검증
 * 
 * 보안 고려사항:
 * - 토큰 만료 시간 적절히 설정
 * - 리프레시 토큰을 통한 토큰 갱신
 * - 서비스 간 토큰 검증 강화
 * 
 * @author Team-FOG
 * @version 1.0
 */
@Service
public class JwtService {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    
    private final JwtConfig jwtConfig;
    private final SecretKey secretKey;
    
    /**
     * JWT 서비스 생성자
     * 
     * @param jwtConfig JWT 설정 정보
     */
    public JwtService(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        // 시크릿 키를 바이트 배열로 변환하여 HMAC-SHA256 서명에 사용
        this.secretKey = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes());
    }
    
    // =============================================================================
    // 토큰 생성 메서드
    // =============================================================================
    
    /**
     * 사용자 정보로 JWT 액세스 토큰을 생성합니다. (MSA 환경용)
     * 
     * 이 메서드는 로그인 성공 시 호출되어 사용자에게 액세스 토큰을 제공합니다.
     * 
     * 토큰에 포함되는 정보:
     * - userId: 사용자 식별자
     * - userName: 사용자 이름
     * - serviceName: 토큰을 발급한 서비스명
     * - tokenType: "ACCESS" (액세스 토큰임을 명시)
     * - issuedAt: 토큰 발급 시간
     * - expiration: 토큰 만료 시간
     * 
     * @param userId 사용자 ID
     * @param userName 사용자 이름
     * @param serviceName 토큰을 발급하는 서비스명
     * @return 생성된 JWT 토큰 문자열
     * 
     * 사용 예시:
     * String token = jwtService.generateToken("user123", "홍길동", "restaurant-reservation-service");
     * 
     * @throws RuntimeException 토큰 생성 실패 시
     */
    public String generateToken(String userId, String userName, String serviceName) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + jwtConfig.getExpiration());
            
            // 토큰에 포함할 정보를 Map으로 구성
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", userId);
            claims.put("userName", userName);
            claims.put("serviceName", serviceName);
            claims.put("tokenType", "ACCESS");
            
            // JWT 토큰 빌더를 사용하여 토큰 생성
            return Jwts.builder()
                    .setClaims(claims)                    // 사용자 정의 정보
                    .setIssuer(jwtConfig.getIssuer())     // 토큰 발급자
                    .setSubject(userId)                   // 토큰 주체 (사용자 ID)
                    .setIssuedAt(now)                     // 발급 시간
                    .setExpiration(expiryDate)            // 만료 시간
                    .signWith(secretKey, Jwts.SIG.HS256)  // HMAC-SHA256 서명
                    .compact();                           // 토큰 문자열 생성
                    
        } catch (Exception e) {
            logger.error("JWT 토큰 생성 중 오류 발생", e);
            throw new RuntimeException("토큰 생성 실패", e);
        }
    }
    
    /**
     * 리프레시 토큰을 생성합니다. (MSA 환경용)
     * 
     * 리프레시 토큰은 액세스 토큰을 갱신하는 용도로 사용됩니다.
     * 액세스 토큰보다 긴 만료 시간을 가지며, 토큰 갱신 시에만 사용됩니다.
     * 
     * 리프레시 토큰의 특징:
     * - 액세스 토큰보다 긴 만료 시간 (기본 12시간)
     * - 토큰 타입이 "REFRESH"로 설정
     * - 사용자 이름은 포함하지 않음 (보안상)
     * 
     * @param userId 사용자 ID
     * @param serviceName 토큰을 발급하는 서비스명
     * @return 생성된 리프레시 토큰 문자열
     * 
     * 사용 예시:
     * String refreshToken = jwtService.generateRefreshToken("user123", "restaurant-reservation-service");
     */
    public String generateRefreshToken(String userId, String serviceName) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + jwtConfig.getRefreshExpiration());
            
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", userId);
            claims.put("serviceName", serviceName);
            claims.put("tokenType", "REFRESH");
            
            return Jwts.builder()
                    .setClaims(claims)
                    .setIssuer(jwtConfig.getIssuer())
                    .setSubject(userId)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(secretKey, Jwts.SIG.HS256)
                    .compact();
                    
        } catch (Exception e) {
            logger.error("리프레시 토큰 생성 중 오류 발생", e);
            throw new RuntimeException("리프레시 토큰 생성 실패", e);
        }
    }
    
    /**
     * 서비스 간 통신을 위한 서비스 토큰을 생성합니다. (MSA 환경용)
     * 
     * 이 메서드는 다른 마이크로서비스와의 통신을 위해 서비스 토큰을 생성합니다.
     * 
     * 토큰에 포함되는 정보:
     * - serviceName: 토큰을 발급한 서비스명
     * - targetService: 대상 서비스명
     * - tokenType: "SERVICE" (서비스 토큰임을 명시)
     * - issuedAt: 토큰 발급 시간
     * - expiration: 토큰 만료 시간 (서비스 토큰은 짧은 만료 시간)
     * 
     * @param targetService 대상 서비스명
     * @return 생성된 서비스 토큰 문자열
     * 
     * 사용 예시:
     * String serviceToken = jwtService.generateServiceToken("restaurant-menu-service");
     * 
     * @throws RuntimeException 토큰 생성 실패 시
     */
    public String generateServiceToken(String targetService) {
        try {
            Date now = new Date();
            // 서비스 토큰은 짧은 만료 시간 (5분)
            Date expiryDate = new Date(now.getTime() + 300000); // 5분
            
            // 토큰에 포함할 정보를 Map으로 구성
            Map<String, Object> claims = new HashMap<>();
            claims.put("serviceName", jwtConfig.getIssuer()); // 발급 서비스
            claims.put("targetService", targetService); // 대상 서비스
            claims.put("tokenType", "SERVICE");
            
            // JWT 토큰 생성
            String token = Jwts.builder()
                    .setClaims(claims)
                    .setIssuer(jwtConfig.getIssuer())
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(secretKey, Jwts.SIG.HS256)
                    .compact();
            
            logger.debug("서비스 토큰 생성 완료: targetService={}", targetService);
            return token;
            
        } catch (Exception e) {
            logger.error("서비스 토큰 생성 실패: targetService={}", targetService, e);
            throw new RuntimeException("서비스 토큰 생성 실패", e);
        }
    }
    
    // =============================================================================
    // 토큰 파싱 메서드
    // =============================================================================
    
    /**
     * JWT 토큰에서 사용자 ID를 추출합니다.
     * 
     * @param token JWT 토큰 문자열
     * @return 사용자 ID, 토큰이 유효하지 않으면 null
     * 
     * 사용 예시:
     * String userId = jwtService.getUserIdFromToken(token);
     * if (userId != null) {
     *     // 사용자 ID로 작업 수행
     * }
     */
    public String getUserIdFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.get("userId", String.class);
        } catch (Exception e) {
            logger.error("토큰에서 사용자 ID 추출 중 오류 발생", e);
            return null;
        }
    }
    
    /**
     * JWT 토큰에서 사용자 이름을 추출합니다.
     * 
     * @param token JWT 토큰 문자열
     * @return 사용자 이름, 토큰이 유효하지 않으면 null
     */
    public String getUserNameFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.get("userName", String.class);
        } catch (Exception e) {
            logger.error("토큰에서 사용자 이름 추출 중 오류 발생", e);
            return null;
        }
    }
    
    /**
     * JWT 토큰에서 서비스 이름을 추출합니다. (MSA용)
     * 
     * MSA 환경에서 어떤 서비스가 토큰을 발급했는지 확인하는 용도로 사용됩니다.
     * 
     * @param token JWT 토큰 문자열
     * @return 서비스 이름, 토큰이 유효하지 않으면 null
     * 
     * 사용 예시:
     * String serviceName = jwtService.getServiceNameFromToken(token);
     * if (jwtConfig.isAllowedService(serviceName)) {
     *     // 허용된 서비스에서 발급된 토큰 처리
     * }
     */
    public String getServiceNameFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.get("serviceName", String.class);
        } catch (Exception e) {
            logger.error("토큰에서 서비스 이름 추출 중 오류 발생", e);
            return null;
        }
    }
    
    // =============================================================================
    // 토큰 검증 메서드
    // =============================================================================
    
    /**
     * JWT 토큰의 유효성을 검증합니다. (MSA 환경 고려)
     * 
     * 검증 항목:
     * 1. 토큰 서명 검증 (위조 방지)
     * 2. 토큰 만료 시간 확인
     * 3. 토큰 형식 검증
     * 4. MSA 환경에서 서비스 검증 (허용된 서비스인지 확인)
     * 
     * @param token 검증할 JWT 토큰 문자열
     * @return 토큰이 유효하면 true, 그렇지 않으면 false
     * 
     * 사용 예시:
     * if (jwtService.validateToken(token)) {
     *     // 토큰이 유효하므로 인증된 요청으로 처리
     *     String userId = jwtService.getUserIdFromToken(token);
     * } else {
     *     // 토큰이 유효하지 않으므로 인증 실패 처리
     * }
     */
    public boolean validateToken(String token) {
        try {
            // 토큰 파싱 및 서명 검증
            Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            
            // MSA 환경에서 서비스 검증
            String serviceName = claims.get("serviceName", String.class);
            if (serviceName != null && !jwtConfig.isAllowedService(serviceName)) {
                logger.warn("허용되지 않은 서비스의 토큰: {}", serviceName);
                return false;
            }
            
            return true;
        } catch (ExpiredJwtException e) {
            logger.warn("만료된 JWT 토큰: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            logger.warn("지원되지 않는 JWT 토큰: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            logger.warn("잘못된 JWT 토큰: {}", e.getMessage());
            return false;
        } catch (SecurityException e) {
            logger.warn("JWT 토큰 서명 검증 실패: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            logger.warn("JWT 토큰이 비어있음: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * JWT 토큰에서 Claims를 추출합니다.
     * 
     * 이 메서드는 내부적으로만 사용되며, 토큰 파싱의 공통 로직을 담당합니다.
     * 
     * @param token JWT 토큰 문자열
     * @return 토큰의 Claims 객체
     * @throws JwtException 토큰 파싱 실패 시
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    // =============================================================================
    // 토큰 정보 조회 메서드
    // =============================================================================
    
    /**
     * 토큰의 만료 시간을 확인합니다.
     * 
     * @param token 확인할 JWT 토큰 문자열
     * @return 토큰이 만료되었으면 true, 그렇지 않으면 false
     * 
     * 사용 예시:
     * if (jwtService.isTokenExpired(token)) {
     *     // 토큰이 만료되었으므로 갱신 필요
     *     // 리프레시 토큰을 사용하여 새로운 액세스 토큰 발급
     * }
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            logger.error("토큰 만료 확인 중 오류 발생", e);
            return true; // 오류 발생 시 만료된 것으로 처리
        }
    }
    
    /**
     * 토큰이 리프레시 토큰인지 확인합니다.
     * 
     * @param token 확인할 JWT 토큰 문자열
     * @return 리프레시 토큰이면 true, 그렇지 않으면 false
     * 
     * 사용 예시:
     * if (jwtService.isRefreshToken(token)) {
     *     // 리프레시 토큰이므로 새로운 액세스 토큰 발급
     *     String userId = jwtService.getUserIdFromToken(token);
     *     String newAccessToken = jwtService.generateToken(userId, "", serviceName);
     * }
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            String tokenType = claims.get("tokenType", String.class);
            return "REFRESH".equals(tokenType);
        } catch (Exception e) {
            logger.error("토큰 타입 확인 중 오류 발생", e);
            return false;
        }
    }
    
    /**
     * 토큰의 남은 유효 시간을 반환합니다. (밀리초)
     * 
     * @param token 확인할 JWT 토큰 문자열
     * @return 남은 유효 시간 (밀리초), 토큰이 유효하지 않으면 0
     * 
     * 사용 예시:
     * long remainingTime = jwtService.getTokenRemainingTime(token);
     * if (remainingTime < 300000) { // 5분 미만
     *     // 토큰이 곧 만료되므로 갱신 권장
     * }
     */
    public long getTokenRemainingTime(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            Date now = new Date();
            return expiration.getTime() - now.getTime();
        } catch (Exception e) {
            logger.error("토큰 남은 시간 계산 중 오류 발생", e);
            return 0;
        }
    }
}
