package com.restaurant.reservation.config;

import com.restaurant.reservation.entity.User;
import com.restaurant.reservation.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * JWT 인증을 위한 사용자 상세 정보 서비스
 * Spring Security의 UserDetailsService를 구현하여 JWT 토큰에서 추출한 userId로 사용자 정보를 로드합니다.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    
    private final UserRepository userRepository;
    
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        logger.debug("사용자 정보 로드 요청: userId={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("사용자를 찾을 수 없습니다: userId={}", userId);
                    return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId);
                });
        
        if (!user.isActive()) {
            logger.warn("비활성화된 사용자: userId={}", userId);
            throw new UsernameNotFoundException("비활성화된 사용자입니다: " + userId);
        }
        
        // Spring Security UserDetails 객체 생성
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getUserId())
                .password(user.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.isActive())
                .build();
        
        logger.debug("사용자 정보 로드 완료: userId={}", userId);
        return userDetails;
    }
}
