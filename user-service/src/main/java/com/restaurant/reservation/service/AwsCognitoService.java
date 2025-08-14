package com.restaurant.reservation.service;

import com.restaurant.reservation.config.AwsCognitoConfig;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;

/**
 * AWS Cognito 서비스 클래스
 * * AWS Cognito와의 통신을 담당합니다.
 * - 사용자 인증
 * - 토큰 검증
 * - 사용자 정보 조회
 * * @author Team-FOG
 * @version 1.0
 * @since 2024-01-15
 */
@Service
public class AwsCognitoService {

    private static final Logger logger = LoggerFactory.getLogger(AwsCognitoService.class);

    private final AwsCognitoConfig cognitoConfig;
    private final RestTemplate restTemplate;
    private final CognitoIdentityProviderClient cognitoClient;

    public AwsCognitoService(AwsCognitoConfig cognitoConfig, RestTemplate restTemplate) {
        this.cognitoConfig = cognitoConfig;
        this.restTemplate = restTemplate;
        this.cognitoClient = CognitoIdentityProviderClient.builder()
                .region(Region.of(cognitoConfig.getRegion()))
                .build();
    }

    /**
     * Cognito에 사용자 회원가입
     */
    public void signUp(String userId, String phoneNumber) {
        try {
            AttributeType phoneNumberAttribute = AttributeType.builder()
                    .name("phone_number")
                    .value(phoneNumber)
                    .build();

            SignUpRequest signUpRequest = SignUpRequest.builder()
                    .clientId(cognitoConfig.getClientId())
                    .username(userId)
                    .userAttributes(phoneNumberAttribute)
                    .build();

            cognitoClient.signUp(signUpRequest);
            logger.info("Cognito 사용자 회원가입 요청 성공: userId={}", userId);
        } catch (Exception e) {
            logger.error("Cognito 사용자 회원가입 실패", e);
            throw new RuntimeException("Cognito 사용자 회원가입에 실패했습니다.", e);
        }
    }

    /**
     * Cognito 로그인 URL 생성
     */
    public String generateLoginUrl(String state) {
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
            // JWT 라이브러리를 사용하여 토큰을 디코딩
            DecodedJWT jwt = JWT.decode(idToken);
            Map<String, Object> claims = new HashMap<>();

            // 토큰 페이로드의 모든 클레임을 맵에 저장
            jwt.getClaims().forEach((key, value) -> claims.put(key, value.asString()));

            return claims;

        } catch (Exception e) {
            logger.error("사용자 정보 조회 중 오류 발생", e);
            throw new RuntimeException("사용자 정보 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            // JWKS 엔드포인트에서 공개 키를 가져옴
            JwkProvider jwkProvider = new JwkProviderBuilder(new URL(cognitoConfig.getJwksUrl())).build();
            DecodedJWT jwt = JWT.decode(token);

            String keyId = jwt.getKeyId();
            if (keyId == null || keyId.isEmpty()) {
                logger.error("JWT 토큰에 kid(Key ID)가 없습니다.");
                return false;
            }

            Jwk jwk = jwkProvider.get(keyId);
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);

            // Corrected: Build the standard Cognito issuer URL
            String expectedIssuer = String.format("https://cognito-idp.%s.amazonaws.com/%s",
                    cognitoConfig.getRegion(),
                    cognitoConfig.getUserPoolId());
            logger.info("Expected issuer: {}", expectedIssuer);

            // JWT 토큰 검증기 생성 및 검증
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(expectedIssuer)
                    .build();
            verifier.verify(token);

            return true;
        } catch (JwkException e) {
            logger.error("JWK Provider에서 키를 가져오는 데 실패했습니다.", e);
            return false;
        } catch (Exception e) {
            logger.error("토큰 검증 실패", e);
            return false;
        }
    }
}