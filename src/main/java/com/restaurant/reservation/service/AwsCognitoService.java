package com.restaurant.reservation.service;

import com.restaurant.reservation.config.AwsCognitoConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;

/**
 * AWS Cognito 서비스 클래스
 * 
 * AWS Cognito와의 통신을 담당합니다.
 * - 사용자 인증
 * - 토큰 검증
 * - 사용자 정보 조회
 * 
 * @author Team-FOG
 * @version 1.0
 * @since 2024-01-15
 */
@Service
public class AwsCognitoService {
    
    private static final Logger logger = LoggerFactory.getLogger(AwsCognitoService.class);
    
    private final AwsCognitoConfig cognitoConfig;
    private final RestTemplate restTemplate;
    
    public AwsCognitoService(AwsCognitoConfig cognitoConfig) {
        this.cognitoConfig = cognitoConfig;
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Cognito 로그인 URL 생성
     */
    public String generateLoginUrl(String state) {
        if (cognitoConfig.isDummyMode()) {
            // 더미 모드: 로컬 테스트용 URL 생성
            String dummyUrl = "http://localhost:8082/api/users/login/dummy?state=" + state;
            logger.info("더미 모드 로그인 URL 생성: {}", dummyUrl);
            return dummyUrl;
        }
        
        String loginUrl = cognitoConfig.getAuthorizeEndpoint() +
                "?response_type=" + cognitoConfig.getResponseType() +
                "&client_id=" + cognitoConfig.getClientId() +
                "&redirect_uri=" + cognitoConfig.getRedirectUri() +
                "&scope=" + cognitoConfig.getScope() +
                "&state=" + state;
        
        logger.info("Cognito 로그인 URL 생성: {}", loginUrl);
        return loginUrl;
    }
    
    /**
     * 인증 코드로 액세스 토큰 교환
     */
    public Map<String, Object> exchangeCodeForToken(String authorizationCode) {
        if (cognitoConfig.isDummyMode()) {
            // 더미 모드: 더미 토큰 생성
            logger.info("더미 모드 토큰 교환: code={}", authorizationCode);
            return generateDummyTokenResponse();
        }
        
        try {
            logger.info("인증 코드로 토큰 교환 시작: code={}", authorizationCode);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", cognitoConfig.getGrantType());
            body.add("client_id", cognitoConfig.getClientId());
            body.add("client_secret", cognitoConfig.getClientSecret());
            body.add("code", authorizationCode);
            body.add("redirect_uri", cognitoConfig.getRedirectUri());
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                cognitoConfig.getTokenEndpoint(), 
                request, 
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> tokenResponse = response.getBody();
                logger.info("토큰 교환 성공: access_token 존재={}", tokenResponse.containsKey("access_token"));
                return tokenResponse;
            } else {
                logger.error("토큰 교환 실패: status={}", response.getStatusCode());
                throw new RuntimeException("토큰 교환에 실패했습니다.");
            }
            
        } catch (Exception e) {
            logger.error("토큰 교환 중 오류 발생", e);
            throw new RuntimeException("토큰 교환 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 리프레시 토큰으로 액세스 토큰 갱신
     */
    public Map<String, Object> refreshToken(String refreshToken) {
        try {
            logger.info("리프레시 토큰으로 액세스 토큰 갱신 시작");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "refresh_token");
            body.add("client_id", cognitoConfig.getClientId());
            body.add("client_secret", cognitoConfig.getClientSecret());
            body.add("refresh_token", refreshToken);
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                cognitoConfig.getTokenEndpoint(), 
                request, 
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> tokenResponse = response.getBody();
                logger.info("토큰 갱신 성공: access_token 존재={}", tokenResponse.containsKey("access_token"));
                return tokenResponse;
            } else {
                logger.error("토큰 갱신 실패: status={}", response.getStatusCode());
                throw new RuntimeException("토큰 갱신에 실패했습니다.");
            }
            
        } catch (Exception e) {
            logger.error("토큰 갱신 중 오류 발생", e);
            throw new RuntimeException("토큰 갱신 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 로그아웃 URL 생성
     */
    public String generateLogoutUrl(String idToken) {
        String logoutUrl = cognitoConfig.getLogoutEndpoint() +
                "?client_id=" + cognitoConfig.getClientId() +
                "&logout_uri=" + cognitoConfig.getRedirectUri() +
                "&id_token_hint=" + idToken;
        
        logger.info("Cognito 로그아웃 URL 생성: {}", logoutUrl);
        return logoutUrl;
    }
    
    /**
     * 사용자 정보 조회 (ID 토큰에서)
     */
    public Map<String, Object> getUserInfoFromIdToken(String idToken) {
        try {
            // ID 토큰을 디코딩하여 사용자 정보 추출
            // 실제 구현에서는 JWT 라이브러리를 사용하여 토큰을 검증하고 디코딩
            logger.info("ID 토큰에서 사용자 정보 조회");
            
            if (cognitoConfig.isDummyMode()) {
                // 더미 모드: 더미 사용자 정보 반환
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("sub", "dummy4879"); // 실제 생성된 더미 사용자 ID
                userInfo.put("email", "dummy@example.com");
                userInfo.put("name", "더미 사용자");
                
                return userInfo;
            }
            
            // 임시 구현 - 실제로는 JWT 디코딩 필요
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("sub", "user123"); // 사용자 고유 ID
            userInfo.put("email", "user@example.com");
            userInfo.put("name", "사용자");
            
            return userInfo;
            
        } catch (Exception e) {
            logger.error("사용자 정보 조회 중 오류 발생", e);
            throw new RuntimeException("사용자 정보 조회 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 더미 토큰 응답 생성
     */
    private Map<String, Object> generateDummyTokenResponse() {
        Map<String, Object> tokenResponse = new HashMap<>();
        tokenResponse.put("access_token", "dummy-access-token-" + System.currentTimeMillis());
        tokenResponse.put("id_token", "dummy-id-token-" + System.currentTimeMillis());
        tokenResponse.put("refresh_token", "dummy-refresh-token-" + System.currentTimeMillis());
        tokenResponse.put("token_type", "Bearer");
        tokenResponse.put("expires_in", 3600);
        return tokenResponse;
    }
    
    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        if (cognitoConfig.isDummyMode()) {
            // 더미 모드: 더미 토큰 검증
            return token != null && token.startsWith("dummy-");
        }
        
        try {
            // 실제 구현에서는 JWT 라이브러리를 사용하여 토큰 검증
            logger.info("토큰 유효성 검증");
            
            // 임시 구현 - 실제로는 JWT 검증 필요
            return token != null && !token.isEmpty();
            
        } catch (Exception e) {
            logger.error("토큰 검증 중 오류 발생", e);
            return false;
        }
    }
}
