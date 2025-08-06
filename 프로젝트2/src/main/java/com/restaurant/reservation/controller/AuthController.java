package com.restaurant.reservation.controller;

import com.restaurant.reservation.dto.UserDto;
import com.restaurant.reservation.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    public ResponseEntity<UserDto> signup(@RequestBody Map<String, String> signupRequest) {
        try {
            logger.info("회원가입 요청: {}", signupRequest);
            
            String userId = signupRequest.get("userId");
            String userName = signupRequest.get("userName");
            String phoneNumber = signupRequest.get("phoneNumber");
            String userLocation = signupRequest.get("userLocation");
            String password = signupRequest.get("password");
            
            if (userName == null || userName.trim().isEmpty()) {
                logger.warn("회원가입 실패: 사용자 이름이 비어있습니다.");
                return ResponseEntity.badRequest().build();
            }
            
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                logger.warn("회원가입 실패: 전화번호가 비어있습니다.");
                return ResponseEntity.badRequest().build();
            }
            
            if (password == null || password.trim().isEmpty()) {
                logger.warn("회원가입 실패: 비밀번호가 비어있습니다.");
                return ResponseEntity.badRequest().build();
            }
            
            UserDto userDto = new UserDto();
            userDto.setUserId(userId != null && !userId.trim().isEmpty() ? userId : "USER" + System.currentTimeMillis());
            userDto.setUserName(userName);
            userDto.setPhoneNumber(phoneNumber);
            userDto.setUserLocation(userLocation != null ? userLocation : "");
            userDto.setPassword(password);
            
            UserDto createdUser = userService.createUser(userDto);
            logger.info("회원가입 완료: userId={}", createdUser.getUserId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (Exception e) {
            logger.error("회원가입 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody Map<String, String> loginRequest, HttpSession session) {
        try {
            logger.info("로그인 API 호출됨: {}", loginRequest);
            
            String userId = loginRequest.get("userId");
            String password = loginRequest.get("password");
            
            if (userId == null || password == null) {
                logger.warn("로그인 실패: 필수 정보 누락");
                return ResponseEntity.badRequest().build();
            }
            
            UserDto authenticatedUser = userService.authenticateUser(userId, password);
            
            if (authenticatedUser != null) {
                session.setAttribute("userId", authenticatedUser.getUserId());
                session.setAttribute("userName", authenticatedUser.getUserName());
                
                logger.info("로그인 성공: userId={}", authenticatedUser.getUserId());
                return ResponseEntity.ok(authenticatedUser);
            } else {
                logger.warn("로그인 실패: 인증 실패");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            logger.error("로그인 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/logout")
    @GetMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        try {
            logger.info("로그아웃 요청");
            session.invalidate();
            logger.info("로그아웃 완료");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("로그아웃 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(HttpSession session) {
        try {
            logger.info("현재 사용자 정보 조회 요청");
            
            String userId = (String) session.getAttribute("userId");
            
            if (userId == null) {
                logger.warn("세션에 사용자 정보가 없음");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            UserDto user = userService.getUserById(userId);
            if (user != null) {
                logger.info("현재 사용자 정보 조회 완료: userId={}", user.getUserId());
                return ResponseEntity.ok(user);
            } else {
                logger.warn("사용자 정보를 찾을 수 없음: userId={}", userId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("현재 사용자 정보 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/api/auth/check-userid")
    public ResponseEntity<Boolean> checkUserIdDuplicate(@RequestParam String userId) {
        try {
            logger.info("아이디 중복 확인 요청: userId={}", userId);
            
            boolean isDuplicate = userService.isUserIdDuplicate(userId);
            logger.info("아이디 중복 확인 완료: userId={}, isDuplicate={}", userId, isDuplicate);
            return ResponseEntity.ok(isDuplicate);
        } catch (Exception e) {
            logger.error("아이디 중복 확인 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 