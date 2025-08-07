package com.restaurant.reservation.controller;

import com.restaurant.reservation.dto.SignupRequestDto;
import com.restaurant.reservation.dto.SignupResponseDto;
import com.restaurant.reservation.dto.UserInfoDto;
import com.restaurant.reservation.entity.User;
import com.restaurant.reservation.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 사용자 관리 컨트롤러
 * 
 * 이 클래스는 사용자 관련 모든 HTTP 요청을 처리합니다.
 * - 회원가입
 * - 내 정보 조회
 * - 전체 사용자 수 조회
 * 
 * @author FOG Team
 * @version 1.0
 * @since 2024-01-15
 */
@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*") // CORS 설정 - 모든 도메인에서 접근 허용
public class UserController {
    
    // 로깅을 위한 Logger 인스턴스
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    // 사용자 서비스 의존성 주입
    private final UserService userService;
    
    /**
     * 생성자 - 의존성 주입
     * @param userService 사용자 서비스
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * 내 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfoDto> getMyInfo(HttpSession session) {
        try {
            logger.info("내 정보 조회 요청");
            
            String userId = (String) session.getAttribute("userId");
            if (userId == null) {
                logger.warn("로그인이 필요합니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            User user = userService.getUserInfo(userId);
            UserInfoDto userInfo = new UserInfoDto(
                user.getUserId(), user.getUserName(), user.getPhoneNumber(), 
                user.getUserLocation(), user.isActive(), user.getCreatedAt(), user.getUpdatedAt()
            );
            
            logger.info("내 정보 조회 완료: userId={}", userId);
            return ResponseEntity.ok(userInfo);
            
        } catch (Exception e) {
            logger.error("내 정보 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 회원가입 처리
     */
    @PostMapping
    public ResponseEntity<SignupResponseDto> signup(@RequestBody SignupRequestDto signupRequest) {
        try {
            logger.info("회원가입 요청: userName={}", signupRequest.getUserName());
            
            User user = userService.signup(
                signupRequest.getUserId(),
                signupRequest.getUserName(),
                signupRequest.getPhoneNumber(),
                signupRequest.getUserLocation(),
                signupRequest.getPassword()
            );
            
            SignupResponseDto signupResponse = new SignupResponseDto(
                user.getUserId(), user.getUserName(), user.getPhoneNumber(),
                user.getUserLocation(), user.getCreatedAt()
            );
            
            logger.info("회원가입 완료: userId={}", signupResponse.getUserId());
            return ResponseEntity.status(HttpStatus.CREATED).body(signupResponse);
            
        } catch (Exception e) {
            logger.error("회원가입 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

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
}
