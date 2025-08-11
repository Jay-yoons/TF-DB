package com.restaurant.reservation.controller;

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

@RestController
@RequestMapping("/login")
@CrossOrigin(origins = "*")
public class LoginController {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    
    private final UserService userService;
    
    public LoginController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * 로그인 처리
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest, HttpSession session) {
        try {
            String userId = loginRequest.get("userId");
            String password = loginRequest.get("password");
            
            logger.info("로그인 요청: userId={}", userId);
            
            if (userId == null || password == null) {
                logger.warn("로그인 정보가 누락되었습니다");
                return ResponseEntity.badRequest().build();
            }
            
            User user = userService.login(userId, password);
            
            // 세션에 사용자 정보 저장
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("userName", user.getUserName());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", user.getUserId());
            response.put("userName", user.getUserName());
            response.put("message", "로그인 성공");
            
            logger.info("로그인 완료: userId={}", user.getUserId());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("로그인 중 오류 발생", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
    
    /**
     * 로그아웃 처리
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        try {
            logger.info("로그아웃 요청");
            
            // 세션 무효화
            session.invalidate();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "로그아웃 성공");
            
            logger.info("로그아웃 완료");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("로그아웃 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 로그인 상태 확인
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> checkLoginStatus(HttpSession session) {
        try {
            String userId = (String) session.getAttribute("userId");
            String userName = (String) session.getAttribute("userName");
            
            Map<String, Object> response = new HashMap<>();
            
            if (userId != null && userName != null) {
                response.put("loggedIn", true);
                response.put("userId", userId);
                response.put("userName", userName);
            } else {
                response.put("loggedIn", false);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("로그인 상태 확인 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
