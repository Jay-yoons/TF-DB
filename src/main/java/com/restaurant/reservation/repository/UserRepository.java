package com.restaurant.reservation.repository;

import com.restaurant.reservation.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    // 로그인용 - userId와 password만 조회
    @Query("SELECT u.userId, u.password FROM User u WHERE u.userId = :userId")
    Optional<Object[]> findUserIdAndPasswordByUserId(@Param("userId") String userId);
    
    // 회원가입용 - userName, phoneNumber만 조회
    @Query("SELECT u.userName, u.phoneNumber FROM User u WHERE u.userName = :userName OR u.phoneNumber = :phoneNumber")
    Optional<Object[]> findUserNameAndPhoneNumberByUserNameOrPhoneNumber(@Param("userName") String userName, @Param("phoneNumber") String phoneNumber);
    
    // 사용자 정보 조회용 - password 제외
    @Query("SELECT u.userId, u.userName, u.phoneNumber, u.userLocation, u.createdAt FROM User u WHERE u.userId = :userId")
    Optional<Object[]> findUserInfoByUserId(@Param("userId") String userId);
    
    // 중복 확인용
    boolean existsByUserId(String userId);
    boolean existsByPhoneNumber(String phoneNumber);
    
    // 전화번호로 사용자 조회
    Optional<User> findByPhoneNumber(String phoneNumber);
} 