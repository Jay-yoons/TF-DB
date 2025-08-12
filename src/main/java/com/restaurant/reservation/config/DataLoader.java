package com.restaurant.reservation.config;

import com.restaurant.reservation.entity.User;
import com.restaurant.reservation.entity.FavoriteStore;
import com.restaurant.reservation.repository.UserRepository;
import com.restaurant.reservation.repository.FavoriteStoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    private final UserRepository userRepository;
    private final FavoriteStoreRepository favoriteStoreRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository, FavoriteStoreRepository favoriteStoreRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.favoriteStoreRepository = favoriteStoreRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("User Service 샘플 데이터 로딩 시작");
        
        // 기존 사용자 데이터 삭제
        userRepository.deleteAll();
        
        // 샘플 사용자 데이터 생성
        User user1 = new User();
        user1.setUserId("user001");
        user1.setUserName("임채이");
        user1.setPhoneNumber("010-1234-5678");
        user1.setUserLocation("서울시 강남구");
        user1.setPassword(passwordEncoder.encode("password123"));
        user1.setActive(true);
        userRepository.save(user1);
        
        User user2 = new User();
        user2.setUserId("user002");
        user2.setUserName("임채삼");
        user2.setPhoneNumber("010-2345-6789");
        user2.setUserLocation("서울시 서초구");
        user2.setPassword(passwordEncoder.encode("password456"));
        user2.setActive(true);
        userRepository.save(user2);
        
        User user3 = new User();
        user3.setUserId("user003");
        user3.setUserName("임채사");
        user3.setPhoneNumber("010-3456-7890");
        user3.setUserLocation("서울시 송파구");
        user3.setPassword(passwordEncoder.encode("password789"));
        user3.setActive(true);
        userRepository.save(user3);
        
        // 기존 즐겨찾기 데이터 삭제
        favoriteStoreRepository.deleteAll();
        
        // 샘플 즐겨찾기 데이터 생성
        FavoriteStore fav1 = new FavoriteStore("user001", "store001");
        favoriteStoreRepository.save(fav1);
        
        FavoriteStore fav2 = new FavoriteStore("user001", "store002");
        favoriteStoreRepository.save(fav2);
        
        FavoriteStore fav3 = new FavoriteStore("user002", "store003");
        favoriteStoreRepository.save(fav3);
        
        FavoriteStore fav4 = new FavoriteStore("user003", "store001");
        favoriteStoreRepository.save(fav4);
        
        FavoriteStore fav5 = new FavoriteStore("user003", "store004");
        favoriteStoreRepository.save(fav5);
        
        logger.info("User Service 샘플 데이터 로딩 완료: {}명, 즐겨찾기 {}개", userRepository.count(), favoriteStoreRepository.count());
    }
} 