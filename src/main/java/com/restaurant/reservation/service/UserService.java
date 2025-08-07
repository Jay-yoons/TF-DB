package com.restaurant.reservation.service;

import com.restaurant.reservation.entity.User;
import com.restaurant.reservation.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 사용자 서비스 클래스
 * 
 * 이 클래스는 사용자 관련 모든 비즈니스 로직을 처리합니다.
 * - 회원가입 및 로그인 처리
 * - 사용자 정보 관리
 * - 중복 확인 및 유효성 검사
 * 
 * @author FOG Team
 * @version 1.0
 * @since 2024-01-15
 */
@Service
@Transactional // 모든 메서드에 트랜잭션 적용
public class UserService {
    
    // 로깅을 위한 Logger 인스턴스
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    // 사용자 데이터 접근을 위한 Repository
    private final UserRepository userRepository;
    
    // 비밀번호 암호화를 위한 PasswordEncoder
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 생성자 - 의존성 주입
     * @param userRepository 사용자 데이터 접근 객체
     * @param passwordEncoder 비밀번호 암호화 도구
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * 회원가입
     */
    public User signup(String userId, String userName, String phoneNumber, String userLocation, String password) {
        logger.info("회원가입 요청: userId={}, userName={}, phoneNumber={}", userId, userName, phoneNumber);
        
        // 아이디 중복 확인
        if (userRepository.existsById(userId)) {
            throw new RuntimeException("이미 사용 중인 아이디입니다.");
        }
        
        // 전화번호 중복 확인
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new RuntimeException("이미 등록된 전화번호입니다.");
        }
        
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);
        
        // 사용자 생성
        User user = new User();
        user.setUserId(userId);
        user.setUserName(userName);
        user.setPhoneNumber(phoneNumber);
        user.setUserLocation(userLocation);
        user.setPassword(encodedPassword);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        logger.info("회원가입 완료: userId={}", savedUser.getUserId());
        
        return savedUser;
    }
    
    /**
     * 로그인
     */
    public User login(String userId, String password) {
        logger.info("로그인 요청: userId={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("등록되지 않은 아이디입니다."));
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        
        if (!user.isActive()) {
            throw new RuntimeException("비활성화된 계정입니다.");
        }
        
        logger.info("로그인 완료: userId={}", user.getUserId());
        return user;
    }
    
    /**
     * 사용자 정보 조회
     */
    @Transactional(readOnly = true)
    public User getUserInfo(String userId) {
        logger.info("사용자 정보 조회: userId={}", userId);
        
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }
    
    /**
     * 사용자 ID 중복 확인
     */
    @Transactional(readOnly = true)
    public boolean isUserIdDuplicate(String userId) {
        return userRepository.existsById(userId);
    }
    
    /**
     * 전화번호 중복 확인
     */
    @Transactional(readOnly = true)
    public boolean isPhoneNumberDuplicate(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    /**
     * 전체 사용자 수 조회
     */
    public long getUserCount() {
        return userRepository.count();
    }
}
