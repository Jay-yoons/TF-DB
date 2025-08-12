package com.restaurant.reservation.service;

import com.restaurant.reservation.entity.User;
import com.restaurant.reservation.entity.FavoriteStore;
import com.restaurant.reservation.repository.UserRepository;
import com.restaurant.reservation.repository.FavoriteStoreRepository;
import com.restaurant.reservation.dto.FavoriteStoreDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

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
    
    // 즐겨찾기 가게 데이터 접근을 위한 Repository
    private final FavoriteStoreRepository favoriteStoreRepository;
    
    // 비밀번호 암호화를 위한 PasswordEncoder
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 생성자 - 의존성 주입
     * @param userRepository 사용자 데이터 접근 객체
     * @param favoriteStoreRepository 즐겨찾기 가게 데이터 접근 객체
     * @param passwordEncoder 비밀번호 암호화 도구
     */
    public UserService(UserRepository userRepository, FavoriteStoreRepository favoriteStoreRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.favoriteStoreRepository = favoriteStoreRepository;
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

    /**
     * 사용자 정보 수정
     */
    public User updateUserInfo(String userId, java.util.Map<String, String> updateRequest) {
        logger.info("사용자 정보 수정 요청: userId={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 업데이트할 필드들 처리
        if (updateRequest.containsKey("userName")) {
            user.setUserName(updateRequest.get("userName"));
        }
        
        if (updateRequest.containsKey("phoneNumber")) {
            String newPhoneNumber = updateRequest.get("phoneNumber");
            // 전화번호 중복 확인 (자신의 전화번호는 제외)
            if (!newPhoneNumber.equals(user.getPhoneNumber()) && 
                userRepository.existsByPhoneNumber(newPhoneNumber)) {
                throw new RuntimeException("이미 등록된 전화번호입니다.");
            }
            user.setPhoneNumber(newPhoneNumber);
        }
        
        if (updateRequest.containsKey("userLocation")) {
            user.setUserLocation(updateRequest.get("userLocation"));
        }
        
        if (updateRequest.containsKey("password")) {
            String newPassword = updateRequest.get("password");
            user.setPassword(passwordEncoder.encode(newPassword));
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        
        User updatedUser = userRepository.save(user);
        logger.info("사용자 정보 수정 완료: userId={}", updatedUser.getUserId());
        
        return updatedUser;
    }

    // =============================================================================
    // 즐겨찾기 가게 관련 메서드
    // =============================================================================

    /**
     * 사용자의 즐겨찾기 가게 목록 조회
     */
    @Transactional(readOnly = true)
    public List<FavoriteStoreDto> getFavoriteStores(String userId) {
        logger.info("즐겨찾기 가게 목록 조회: userId={}", userId);
        
        List<FavoriteStore> favoriteStores = favoriteStoreRepository.findByUserId(userId);
        List<FavoriteStoreDto> favoriteStoreDtos = new ArrayList<>();
        
        for (FavoriteStore favoriteStore : favoriteStores) {
            // 실제로는 Store Service에서 가게 이름을 가져와야 함
            // 현재는 더미 데이터로 응답
            String storeName = getDummyStoreName(favoriteStore.getStoreId());
            
            FavoriteStoreDto dto = new FavoriteStoreDto(
                favoriteStore.getFavStoreId(),
                favoriteStore.getStoreId(),
                storeName,
                favoriteStore.getUserId(),
                favoriteStore.getCreatedAt()
            );
            favoriteStoreDtos.add(dto);
        }
        
        logger.info("즐겨찾기 가게 목록 조회 완료: userId={}, count={}", userId, favoriteStoreDtos.size());
        return favoriteStoreDtos;
    }

    /**
     * 즐겨찾기 가게 추가
     */
    public FavoriteStoreDto addFavoriteStore(String userId, String storeId) {
        logger.info("즐겨찾기 가게 추가: userId={}, storeId={}", userId, storeId);
        
        // 이미 즐겨찾기한 가게인지 확인
        if (favoriteStoreRepository.findByUserIdAndStoreId(userId, storeId).isPresent()) {
            throw new RuntimeException("이미 즐겨찾기한 가게입니다.");
        }
        
        // 즐겨찾기 가게 생성
        FavoriteStore favoriteStore = new FavoriteStore(userId, storeId);
        FavoriteStore savedFavoriteStore = favoriteStoreRepository.save(favoriteStore);
        
        // 실제로는 Store Service에서 가게 이름을 가져와야 함
        String storeName = getDummyStoreName(storeId);
        
        FavoriteStoreDto dto = new FavoriteStoreDto(
            savedFavoriteStore.getFavStoreId(),
            savedFavoriteStore.getStoreId(),
            storeName,
            savedFavoriteStore.getUserId(),
            savedFavoriteStore.getCreatedAt()
        );
        
        logger.info("즐겨찾기 가게 추가 완료: userId={}, storeId={}", userId, storeId);
        return dto;
    }

    /**
     * 즐겨찾기 가게 삭제
     */
    public void removeFavoriteStore(String userId, String storeId) {
        logger.info("즐겨찾기 가게 삭제: userId={}, storeId={}", userId, storeId);
        
        favoriteStoreRepository.deleteByUserIdAndStoreId(userId, storeId);
        
        logger.info("즐겨찾기 가게 삭제 완료: userId={}, storeId={}", userId, storeId);
    }

    /**
     * 특정 가게가 즐겨찾기되어 있는지 확인
     */
    @Transactional(readOnly = true)
    public boolean isFavoriteStore(String userId, String storeId) {
        return favoriteStoreRepository.findByUserIdAndStoreId(userId, storeId).isPresent();
    }

    /**
     * 사용자의 즐겨찾기 가게 개수 조회
     */
    @Transactional(readOnly = true)
    public long getFavoriteStoreCount(String userId) {
        return favoriteStoreRepository.countByUserId(userId);
    }

    /**
     * 더미 가게 이름 생성 (실제로는 Store Service에서 가져와야 함)
     */
    private String getDummyStoreName(String storeId) {
        switch (storeId) {
            case "store001":
                return "맛있는 한식당";
            case "store002":
                return "신선한 중식당";
            case "store003":
                return "분위기 좋은 카페";
            case "store004":
                return "고급 양식당";
            case "store005":
                return "전통 일본식당";
            default:
                return "알 수 없는 가게";
        }
    }
}
