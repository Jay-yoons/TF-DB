package com.restaurant.reservation.service;

import com.restaurant.reservation.dto.UserDto;
import com.restaurant.reservation.entity.User;
import com.restaurant.reservation.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDto createUser(UserDto userDto) {
        logger.info("사용자 생성 요청: {}", userDto.getUserId());
        
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(userDto.getPassword());
        
        User user = new User();
        user.setUserId(userDto.getUserId());
        user.setUserName(userDto.getUserName());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setUserLocation(userDto.getUserLocation());
        user.setPassword(encodedPassword);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        logger.info("사용자 생성 완료: userId={}", savedUser.getUserId());
        
        return convertToDto(savedUser);
    }

    public UserDto authenticateUser(String userId, String password) {
        logger.info("사용자 인증 요청: userId={}", userId);
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                logger.info("사용자 인증 성공: userId={}", userId);
                return convertToDto(user);
            }
        }
        
        logger.warn("사용자 인증 실패: userId={}", userId);
        return null;
    }

    public UserDto getUserById(String userId) {
        logger.info("사용자 조회: userId={}", userId);
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            logger.info("사용자 조회 완료: userId={}", userId);
            return convertToDto(user);
        }
        
        logger.warn("사용자를 찾을 수 없음: userId={}", userId);
        return null;
    }

    public List<UserDto> getAllUsers() {
        logger.info("모든 사용자 조회");
        
        List<User> users = userRepository.findAll();
        List<UserDto> userDtos = users.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        logger.info("모든 사용자 조회 완료: {}명", userDtos.size());
        return userDtos;
    }

    public boolean isUserIdDuplicate(String userId) {
        logger.info("사용자 ID 중복 확인: userId={}", userId);
        
        boolean isDuplicate = userRepository.existsById(userId);
        logger.info("사용자 ID 중복 확인 완료: userId={}, 중복={}", userId, isDuplicate);
        
        return isDuplicate;
    }

    public boolean isPhoneNumberDuplicate(String phoneNumber) {
        logger.info("전화번호 중복 확인: phoneNumber={}", phoneNumber);
        
        boolean isDuplicate = userRepository.existsByPhoneNumber(phoneNumber);
        logger.info("전화번호 중복 확인 완료: phoneNumber={}, 중복={}", phoneNumber, isDuplicate);
        
        return isDuplicate;
    }

    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setUserId(user.getUserId());
        dto.setUserName(user.getUserName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setUserLocation(user.getUserLocation());
        dto.setActive(user.isActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
} 