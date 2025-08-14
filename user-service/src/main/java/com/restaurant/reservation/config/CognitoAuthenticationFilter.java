package com.restaurant.reservation.config;

import com.restaurant.reservation.service.AwsCognitoService;
import com.restaurant.reservation.config.AwsCognitoConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * AWS Cognito 인증 필터
 * * 요청의 Authorization 헤더에서 Cognito JWT 토큰을 추출하고 검증합니다.
 * * @author Team-FOG
 * @version 1.0
 * @since 2024-01-15
 */
@Component
public class CognitoAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(CognitoAuthenticationFilter.class);

    private final AwsCognitoService cognitoService;
    private final CustomUserDetailsService userDetailsService;

    // 명시적으로 생성자를 정의하여 의존성을 주입
    public CognitoAuthenticationFilter(AwsCognitoService cognitoService, CustomUserDetailsService userDetailsService) {
        this.cognitoService = cognitoService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String token = extractTokenFromRequest(request);

            if (token != null && cognitoService.validateToken(token)) {
                // 토큰이 유효한 경우 사용자 정보를 추출
                Map<String, Object> userInfo = cognitoService.getUserInfoFromIdToken(token);
                String userId = (String) userInfo.get("sub");
                logger.info("토큰에서 추출된 sub - userId={}", userId);

                if (userId != null) {
                    // 실제 모드: 데이터베이스에서 사용자 정보 조회
                    // UserDetailsService를 사용하여 DB에서 사용자 정보를 로드
                    UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
                    logger.info("userDetails 완료");
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    logger.info("authentication 완료");
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("Cognito 인증 성공: userId={}", userId);
                }
            }

        } catch (Exception e) {
            logger.error("Cognito 인증 처리 중 오류 발생", e);
            // 인증 실패 시에도 요청을 계속 진행
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 요청에서 JWT 토큰을 추출
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 제거
        }

        return null;
    }
}