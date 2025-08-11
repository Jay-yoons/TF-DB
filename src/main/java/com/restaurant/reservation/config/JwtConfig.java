package com.restaurant.reservation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MSA 환경을 고려한 JWT 설정 클래스
 * 
 * 이 클래스는 모든 마이크로서비스에서 공통으로 사용할 JWT 설정을 관리합니다.
 * 
 * 주요 기능:
 * 1. JWT 토큰의 시크릿 키 관리
 * 2. 토큰 만료 시간 설정
 * 3. 허용된 서비스 목록 관리
 * 4. 토큰 헤더 및 접두사 설정
 * 
 * 사용 방법:
 * - application.yml에서 jwt.* 설정을 통해 값을 변경할 수 있습니다.
 * - 모든 마이크로서비스에서 동일한 설정을 사용해야 토큰 공유가 가능합니다.
 * 
 * 보안 주의사항:
 * - 실제 운영 환경에서는 시크릿 키를 환경변수나 설정 서버에서 관리하세요.
 * - 시크릿 키는 최소 32자 이상의 복잡한 문자열을 사용하세요.
 * 
 * @author Team-FOG
 * @version 1.0
 */
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    
    // =============================================================================
    // JWT 토큰 기본 설정
    // =============================================================================
    
    /**
     * JWT 토큰 서명에 사용되는 시크릿 키
     * 
     * 중요: 실제 운영 환경에서는 이 값을 환경변수나 설정 서버에서 관리하세요.
     * 예: System.getenv("JWT_SECRET") 또는 Config Server에서 관리
     * 
     * 보안 요구사항:
     * - 최소 32자 이상
     * - 영문, 숫자, 특수문자 조합
     * - 예측 불가능한 랜덤 문자열 권장
     */
    private String secret = "msa-restaurant-reservation-secret-key-2024-team-fog-secure-production-key";
    
    /**
     * 액세스 토큰 만료 시간 (밀리초)
     * 
     * 기본값: 24시간 (86400000ms)
     * 
     * 보안 고려사항:
     * - 너무 길면 보안 위험 증가
     * - 너무 짧으면 사용자 경험 저하
     * - 일반적으로 15분~24시간 사이 권장
     */
    private long expiration = 86400000;
    
    /**
     * JWT 토큰 발급자 (서비스 식별용)
     * 
     * MSA 환경에서 어떤 서비스가 토큰을 발급했는지 식별하는 용도
     * 토큰 검증 시 이 값이 허용된 서비스 목록에 포함되어 있는지 확인
     */
    private String issuer = "restaurant-reservation-service";
    
    /**
     * 리프레시 토큰 만료 시간 (밀리초)
     * 
     * 기본값: 12시간 (43200000ms)
     * 
     * 리프레시 토큰은 액세스 토큰을 갱신하는 용도로 사용됩니다.
     * 액세스 토큰보다 긴 만료 시간을 가집니다.
     */
    private long refreshExpiration = 43200000;
    
    // =============================================================================
    // MSA 환경 설정
    // =============================================================================
    
    /**
     * 허용된 서비스 목록
     * 
     * MSA 환경에서 토큰을 공유할 수 있는 서비스들의 목록입니다.
     * 보안을 위해 허용된 서비스에서만 발급된 토큰을 검증합니다.
     * 
     * 새로운 서비스를 추가할 때:
     * 1. 이 배열에 서비스명 추가
     * 2. 해당 서비스에서도 동일한 시크릿 키 사용
     * 3. 토큰 발급 시 issuer를 해당 서비스명으로 설정
     */
    private String[] allowedServices = {
        "restaurant-reservation-service",  // 예약 서비스
        "restaurant-menu-service",         // 메뉴 서비스
        "restaurant-order-service",        // 주문 서비스
        "restaurant-payment-service",      // 결제 서비스
        "restaurant-notification-service"  // 알림 서비스
    };
    
    // =============================================================================
    // HTTP 헤더 설정
    // =============================================================================
    
    /**
     * JWT 토큰을 전달할 HTTP 헤더 이름
     * 
     * 클라이언트에서 서버로 토큰을 전달할 때 사용하는 헤더명
     * 일반적으로 "Authorization"을 사용합니다.
     */
    private String headerName = "Authorization";
    
    /**
     * JWT 토큰 접두사
     * 
     * 토큰 앞에 붙는 접두사로, 일반적으로 "Bearer "를 사용합니다.
     * 예: "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     */
    private String tokenPrefix = "Bearer ";
    
    // =============================================================================
    // Getter/Setter 메서드
    // =============================================================================
    
    public String getSecret() {
        return secret;
    }
    
    public void setSecret(String secret) {
        this.secret = secret;
    }
    
    public long getExpiration() {
        return expiration;
    }
    
    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }
    
    public String getIssuer() {
        return issuer;
    }
    
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
    
    public long getRefreshExpiration() {
        return refreshExpiration;
    }
    
    public void setRefreshExpiration(long refreshExpiration) {
        this.refreshExpiration = refreshExpiration;
    }
    
    public String[] getAllowedServices() {
        return allowedServices;
    }
    
    public void setAllowedServices(String[] allowedServices) {
        this.allowedServices = allowedServices;
    }
    
    public String getHeaderName() {
        return headerName;
    }
    
    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }
    
    public String getTokenPrefix() {
        return tokenPrefix;
    }
    
    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }
    
    // =============================================================================
    // 유틸리티 메서드
    // =============================================================================
    
    /**
     * 서비스가 허용된 서비스 목록에 포함되어 있는지 확인
     * 
     * @param serviceName 확인할 서비스명
     * @return 허용된 서비스인 경우 true, 그렇지 않으면 false
     * 
     * 사용 예시:
     * if (jwtConfig.isAllowedService("restaurant-menu-service")) {
     *     // 메뉴 서비스에서 발급된 토큰 처리
     * }
     */
    public boolean isAllowedService(String serviceName) {
        if (serviceName == null) {
            return false;
        }
        
        for (String allowedService : allowedServices) {
            if (allowedService.equals(serviceName)) {
                return true;
            }
        }
        return false;
    }
}
