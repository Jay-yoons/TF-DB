package com.restaurant.reservation.service;

import com.restaurant.reservation.dto.favorite.FavoriteRequestDto;
import com.restaurant.reservation.dto.favorite.FavoriteResponseDto;
import com.restaurant.reservation.entity.Favorite;
import com.restaurant.reservation.repository.FavoriteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FavoriteService {
    
    private static final Logger logger = LoggerFactory.getLogger(FavoriteService.class);
    
    private final FavoriteRepository favoriteRepository;
    
    public FavoriteService(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }
    
    /**
     * 즐겨찾기 추가
     */
    public FavoriteResponseDto addFavorite(FavoriteRequestDto requestDto) {
        logger.info("즐겨찾기 추가 요청: userId={}, storeId={}", requestDto.getUserId(), requestDto.getStoreId());
        
        // 이미 즐겨찾기에 추가되어 있는지 확인
        if (favoriteRepository.existsByUserIdAndStoreId(requestDto.getUserId(), requestDto.getStoreId())) {
            logger.warn("이미 즐겨찾기에 추가된 가게입니다: userId={}, storeId={}", requestDto.getUserId(), requestDto.getStoreId());
            throw new RuntimeException("이미 즐겨찾기에 추가된 가게입니다.");
        }
        
        // 즐겨찾기 생성
        Favorite favorite = new Favorite(requestDto.getUserId(), requestDto.getStoreId());
        Favorite savedFavorite = favoriteRepository.save(favorite);
        
        logger.info("즐겨찾기 추가 완료: favStoreId={}", savedFavorite.getFavStoreId());
        
        return new FavoriteResponseDto(
                savedFavorite.getFavStoreId(),
                savedFavorite.getUserId(),
                savedFavorite.getStoreId(),
                null, // storeName - 외부 서비스에서 조회 필요
                null, // storeLocation - 외부 서비스에서 조회 필요
                null, // categoryCode - 외부 서비스에서 조회 필요
                null, // averageRating - 외부 서비스에서 조회 필요
                null, // isOpen - 외부 서비스에서 조회 필요
                savedFavorite.getCreatedAt()
        );
    }
    
    /**
     * 즐겨찾기 삭제
     */
    public void removeFavorite(String userId, String storeId) {
        logger.info("즐겨찾기 삭제 요청: userId={}, storeId={}", userId, storeId);
        
        favoriteRepository.deleteByUserIdAndStoreId(userId, storeId);
        
        logger.info("즐겨찾기 삭제 완료: userId={}, storeId={}", userId, storeId);
    }
    
    /**
     * 사용자의 즐겨찾기 목록 조회 (User Service 내부 데이터만)
     */
    @Transactional(readOnly = true)
    public List<FavoriteResponseDto> getUserFavorites(String userId) {
        logger.info("사용자 즐겨찾기 목록 조회: userId={}", userId);
        
        List<Favorite> favorites = favoriteRepository.findByUserId(userId);
        
        return favorites.stream()
                .map(favorite -> new FavoriteResponseDto(
                        favorite.getFavStoreId(),
                        favorite.getUserId(),
                        favorite.getStoreId(),
                        null, // storeName - 외부 서비스에서 조회 필요
                        null, // storeLocation - 외부 서비스에서 조회 필요
                        null, // categoryCode - 외부 서비스에서 조회 필요
                        null, // averageRating - 외부 서비스에서 조회 필요
                        null, // isOpen - 외부 서비스에서 조회 필요
                        favorite.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
    
    /**
     * 즐겨찾기 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isFavorite(String userId, String storeId) {
        return favoriteRepository.existsByUserIdAndStoreId(userId, storeId);
    }
    
    /**
     * 사용자의 즐겨찾기 가게 ID 목록 조회
     */
    @Transactional(readOnly = true)
    public List<String> getUserFavoriteStoreIds(String userId) {
        return favoriteRepository.findStoreIdsByUserId(userId);
    }
}
