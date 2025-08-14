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
import java.util.Map;
import java.util.HashMap;

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
     * 사용자의 즐겨찾기 가게 목록 조회 (기존 방식 - 더미 데이터 사용)
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
     * 뷰를 사용하여 사용자의 즐겨찾기 가게 상세 정보 조회 (개선된 방식)
     * DB 담당자와 협의 후 사용
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getFavoriteStoresWithDetails(String userId) {
        logger.info("뷰를 사용한 즐겨찾기 가게 상세 정보 조회: userId={}", userId);
        
        try {
            List<Object[]> results = favoriteStoreRepository.findFavoriteStoresWithDetails(userId);
            List<Map<String, Object>> favoriteStores = new ArrayList<>();
            
            for (Object[] row : results) {
                Map<String, Object> store = new HashMap<>();
                store.put("favStoreId", row[0]);
                store.put("userId", row[1]);
                store.put("storeId", row[2]);
                store.put("storeName", row[3]);
                store.put("storeLocation", row[4]);
                store.put("serviceTime", row[5]);
                store.put("categoryCode", row[6]);
                store.put("categoryName", row[7]);
                store.put("seatNum", row[8]);
                store.put("favoriteCreatedAt", row[9]);
                favoriteStores.add(store);
            }
            
            logger.info("뷰를 사용한 즐겨찾기 가게 조회 완료: userId={}, count={}", userId, favoriteStores.size());
            return favoriteStores;
            
        } catch (Exception e) {
            logger.error("뷰 조회 중 오류 발생: userId={}, error={}", userId, e.getMessage());
            // 뷰가 없거나 오류 발생 시 기존 방식으로 fallback
            logger.info("기존 방식으로 fallback: userId={}", userId);
            return getFavoriteStoresFallback(userId);
        }
    }

    /**
     * 뷰를 사용하여 사용자의 리뷰 상세 정보 조회
     * DB 담당자와 협의 후 사용
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getUserReviewsWithDetails(String userId) {
        logger.info("뷰를 사용한 사용자 리뷰 상세 정보 조회: userId={}", userId);
        
        try {
            List<Object[]> results = favoriteStoreRepository.findUserReviewsWithDetails(userId);
            List<Map<String, Object>> reviews = new ArrayList<>();
            
            for (Object[] row : results) {
                Map<String, Object> review = new HashMap<>();
                review.put("reviewId", row[0]);
                review.put("userId", row[1]);
                review.put("storeId", row[2]);
                review.put("storeName", row[3]);
                review.put("storeLocation", row[4]);
                review.put("categoryCode", row[5]);
                review.put("categoryName", row[6]);
                review.put("comment", row[7]);
                review.put("score", row[8]);
                review.put("reviewIdStr", row[9]);
                reviews.add(review);
            }
            
            logger.info("뷰를 사용한 리뷰 조회 완료: userId={}, count={}", userId, reviews.size());
            return reviews;
            
        } catch (Exception e) {
            logger.error("뷰 조회 중 오류 발생: userId={}, error={}", userId, e.getMessage());
            // 뷰가 없거나 오류 발생 시 기존 방식으로 fallback
            logger.info("기존 방식으로 fallback: userId={}", userId);
            return getUserReviewsFallback(userId);
        }
    }

    /**
     * 뷰를 사용하여 사용자의 예약 현황 조회
     * DB 담당자와 협의 후 사용
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getUserBookingsWithDetails(String userId) {
        logger.info("뷰를 사용한 사용자 예약 현황 조회: userId={}", userId);
        
        try {
            List<Object[]> results = favoriteStoreRepository.findUserBookingsWithDetails(userId);
            List<Map<String, Object>> bookings = new ArrayList<>();
            
            for (Object[] row : results) {
                Map<String, Object> booking = new HashMap<>();
                booking.put("bookingNum", row[0]);
                booking.put("userId", row[1]);
                booking.put("storeId", row[2]);
                booking.put("storeName", row[3]);
                booking.put("storeLocation", row[4]);
                booking.put("categoryCode", row[5]);
                booking.put("categoryName", row[6]);
                booking.put("bookingDate", row[7]);
                booking.put("bookingStateCode", row[8]);
                booking.put("stateName", row[9]);
                booking.put("count", row[10]);
                booking.put("inUsingSeat", row[11]);
                booking.put("seatNum", row[12]);
                bookings.add(booking);
            }
            
            logger.info("뷰를 사용한 예약 조회 완료: userId={}, count={}", userId, bookings.size());
            return bookings;
            
        } catch (Exception e) {
            logger.error("뷰 조회 중 오류 발생: userId={}, error={}", userId, e.getMessage());
            // 뷰가 없거나 오류 발생 시 빈 리스트 반환
            return new ArrayList<>();
        }
    }

    /**
     * 뷰를 사용하여 사용자 대시보드 통계 조회
     * DB 담당자와 협의 후 사용
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserDashboardStats(String userId) {
        logger.info("뷰를 사용한 사용자 대시보드 통계 조회: userId={}", userId);
        
        try {
            Object[] result = favoriteStoreRepository.findUserDashboardStats(userId);
            Map<String, Object> dashboard = new HashMap<>();
            
            if (result != null) {
                dashboard.put("userId", result[0]);
                dashboard.put("userName", result[1]);
                dashboard.put("phoneNumber", result[2]);
                dashboard.put("userLocation", result[3]);
                dashboard.put("isActive", result[4]);
                dashboard.put("userCreatedAt", result[5]);
                dashboard.put("favoriteCount", result[6]);
                dashboard.put("reviewCount", result[7]);
                dashboard.put("avgReviewScore", result[8]);
                dashboard.put("totalBookingCount", result[9]);
                dashboard.put("activeBookingCount", result[10]);
                dashboard.put("cancelledBookingCount", result[11]);
            }
            
            logger.info("뷰를 사용한 대시보드 통계 조회 완료: userId={}", userId);
            return dashboard;
            
        } catch (Exception e) {
            logger.error("뷰 조회 중 오류 발생: userId={}, error={}", userId, e.getMessage());
            // 뷰가 없거나 오류 발생 시 기본 정보만 반환
            return getUserDashboardStatsFallback(userId);
        }
    }

    // =============================================================================
    // Fallback 메서드들 (뷰가 없을 때 사용)
    // =============================================================================

    /**
     * 즐겨찾기 가게 조회 fallback (기존 방식)
     */
    private List<Map<String, Object>> getFavoriteStoresFallback(String userId) {
        List<FavoriteStore> favoriteStores = favoriteStoreRepository.findByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (FavoriteStore fs : favoriteStores) {
            Map<String, Object> store = new HashMap<>();
            store.put("favStoreId", fs.getFavStoreId());
            store.put("userId", fs.getUserId());
            store.put("storeId", fs.getStoreId());
            store.put("storeName", getDummyStoreName(fs.getStoreId()));
            store.put("storeLocation", "주소 정보 없음");
            store.put("serviceTime", "영업시간 정보 없음");
            store.put("categoryCode", "UNKNOWN");
            store.put("categoryName", "알 수 없음");
            store.put("seatNum", 0);
            store.put("favoriteCreatedAt", fs.getCreatedAt());
            result.add(store);
        }
        
        return result;
    }

    /**
     * 사용자 리뷰 조회 fallback (기존 방식)
     */
    private List<Map<String, Object>> getUserReviewsFallback(String userId) {
        // Store Service Integration을 사용하여 리뷰 조회
        try {
            // StoreServiceIntegration을 주입받아야 함
            // 현재는 빈 리스트 반환
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("리뷰 조회 fallback 실패: userId={}, error={}", userId, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 사용자 대시보드 통계 fallback (기존 방식)
     */
    private Map<String, Object> getUserDashboardStatsFallback(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        Map<String, Object> dashboard = new HashMap<>();
        
        if (user != null) {
            dashboard.put("userId", user.getUserId());
            dashboard.put("userName", user.getUserName());
            dashboard.put("phoneNumber", user.getPhoneNumber());
            dashboard.put("userLocation", user.getUserLocation());
            dashboard.put("isActive", user.isActive());
            dashboard.put("userCreatedAt", user.getCreatedAt());
            dashboard.put("favoriteCount", favoriteStoreRepository.countByUserId(userId));
            dashboard.put("reviewCount", 0); // Store Service 연동 필요
            dashboard.put("avgReviewScore", 0.0);
            dashboard.put("totalBookingCount", 0); // Reservation Service 연동 필요
            dashboard.put("activeBookingCount", 0);
            dashboard.put("cancelledBookingCount", 0);
        }
        
        return dashboard;
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
