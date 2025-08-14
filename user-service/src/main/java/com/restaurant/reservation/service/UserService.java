package com.restaurant.reservation.service;

import com.restaurant.reservation.dto.FavoriteStoreDto;
import com.restaurant.reservation.dto.SignupResponseDto; // SignupResponseDto import
import com.restaurant.reservation.dto.UserInfoDto; // UserInfoDto import
import com.restaurant.reservation.entity.FavoriteStore;
import com.restaurant.reservation.entity.User;
import com.restaurant.reservation.repository.FavoriteStoreRepository;
import com.restaurant.reservation.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final FavoriteStoreRepository favoriteStoreRepository;
    private final StoreServiceIntegration storeServiceIntegration;

    /**
     * 회원가입 (Cognito에서 사용자 생성 후 DB에 저장)
     * 유효성 검사 후 User 엔티티에 저장합니다.
     */
    @Transactional
    public User signup(String cognitoSub, String userName, String phoneNumber, String userLocation) {
        logger.info("회원가입 요청 (DB 저장): cognitoSub={}, userName={}, phoneNumber={}", cognitoSub, userName, phoneNumber);

        // Cognito의 고유 ID인 cognitoSub를 DB의 userId로 사용
        if (userRepository.existsById(cognitoSub)) {
            // 이 로직은 첫 로그인 시점에만 실행되므로, 이미 존재하는 경우는 발생하기 어려움
            // 만약 발생한다면, 이미 등록된 사용자라는 의미
            throw new RuntimeException("이미 등록된 사용자입니다.");
        }

        // 전화번호 중복 검사는 유지 (사용자가 Cognito에 등록한 전화번호)
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new RuntimeException("이미 등록된 전화번호입니다.");
        }

        // User 객체 생성 시, Cognito의 고유 ID(cognitoSub)를 userId로 사용
        User user = new User(cognitoSub, userName, phoneNumber, userLocation);

        User savedUser = userRepository.save(user);
        logger.info("회원가입 완료: userId={}", savedUser.getUserId());

        return savedUser;
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
     * 사용자 정보 수정
     */
    @Transactional
    public User updateUserInfo(String userId, Map<String, String> updateRequest) {
        logger.info("사용자 정보 수정 요청: userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (updateRequest.containsKey("userName")) {
            user.setUserName(updateRequest.get("userName"));
        }

        if (updateRequest.containsKey("phoneNumber")) {
            String newPhoneNumber = updateRequest.get("phoneNumber");
            if (!newPhoneNumber.equals(user.getPhoneNumber()) &&
                    userRepository.existsByPhoneNumber(newPhoneNumber)) {
                throw new RuntimeException("이미 등록된 전화번호입니다.");
            }
            user.setPhoneNumber(newPhoneNumber);
        }

        if (updateRequest.containsKey("userLocation")) {
            user.setUserLocation(updateRequest.get("userLocation"));
        }

        User updatedUser = userRepository.save(user);
        logger.info("사용자 정보 수정 완료: userId={}", updatedUser.getUserId());

        return updatedUser;
    }

    /**
     * 전체 사용자 수 조회
     */
    @Transactional
    public long getUserCount() {
        return userRepository.count();
    }

    /**
     * 즐겨찾기 가게 목록 조회
     */
    @Transactional(readOnly = true)
    public List<FavoriteStoreDto> getFavoriteStores(String userId) {
        logger.info("즐겨찾기 가게 목록 조회: userId={}", userId);

        List<FavoriteStore> favoriteStores = favoriteStoreRepository.findByUserId(userId);
        List<FavoriteStoreDto> favoriteStoreDtos = new ArrayList<>();

        for (FavoriteStore favoriteStore : favoriteStores) {
            String storeName = storeServiceIntegration.getStoreInfo(favoriteStore.getStoreId()).get("storeName").toString();

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
    @Transactional
    public FavoriteStoreDto addFavoriteStore(String userId, String storeId) {
        logger.info("즐겨찾기 가게 추가: userId={}, storeId={}", userId, storeId);

        if (favoriteStoreRepository.findByUserIdAndStoreId(userId, storeId).isPresent()) {
            throw new RuntimeException("이미 즐겨찾기한 가게입니다.");
        }

        FavoriteStore favoriteStore = new FavoriteStore(userId, storeId);
        FavoriteStore savedFavoriteStore = favoriteStoreRepository.save(favoriteStore);

        String storeName = storeServiceIntegration.getStoreInfo(favoriteStore.getStoreId()).get("storeName").toString();

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
    @Transactional
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

    public Optional<User> findUserByUserId(String userId) {
        return userRepository.findById(userId);
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

    public boolean isUserExists(String userId) {
        return userRepository.existsById(userId);
    }
}