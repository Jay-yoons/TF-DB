# 📊 **DB 담당 팀원 전달 문서: 통합 마이페이지 구현 완료**

## 🎯 **개요**

DB 담당 팀원의 피드백을 바탕으로 **대시보드 개념을 제거하고 마이페이지를 통합된 개인 정보 센터**로 개선했습니다. 이 문서는 구현된 내용과 DB 팀이 수행해야 할 작업을 정리한 것입니다.

## 📋 **DB 팀 피드백 분석 및 해결 방안**

### **1. MV → VIEW 변경 관련 피드백**

#### **피드백 내용**
> "MV로 5분간 UPDATE하는 것은 리소스를 많이 잡아 먹는다는 판단 VIEW로 사용시에 실시간 커넥션이 가능하기에 USER API에서는 문제가 발생 하지 않음"

#### **해결 방안**
- ✅ **VIEW 사용**: Materialized View 대신 일반 View 사용
- ✅ **실시간 데이터**: 실시간 커넥션으로 최신 데이터 제공
- ✅ **리소스 최적화**: 5분마다 업데이트하는 부하 제거

### **2. JOIN 문 최적화 관련 피드백**

#### **피드백 내용**
> "JOIN 문의 경우 VIEW가 리소스를 많이 잡아 먹는다는 판단 하에 STORE_NAME 컬럼을 REVIEW, FAV_STORE에 넣는 것을 진행 하였습니다."

#### **해결 방안**
- ✅ **컬럼 중복**: `STORE_NAME`을 `REVIEW`, `FAV_STORE` 테이블에 추가
- ✅ **JOIN 제거**: 복잡한 JOIN 쿼리 대신 단순 SELECT 사용
- ✅ **성능 향상**: 데이터베이스 부하 감소

## 🔧 **구현된 통합 마이페이지 구조**

### **새로운 API 엔드포인트**
```java
@GetMapping("/api/users/me")
public ResponseEntity<Map<String, Object>> getMyPage()
```

### **응답 구조**
```json
{
  "userInfo": {
    "userId": "user123",
    "userName": "홍길동",
    "phoneNumber": "010-1234-5678",
    "userLocation": "서울시 강남구",
    "isActive": true,
    "createdAt": "2025-08-14T10:30:00",
    "updatedAt": "2025-08-14T10:30:00"
  },
  "statistics": {
    "favoriteCount": 5,
    "reviewCount": 12,
    "totalBookingCount": 8,
    "activeBookingCount": 2
  },
  "recentActivities": {
    "favorites": [
      {
        "storeId": "store1",
        "storeName": "맛있는 식당",
        "createdAt": "2025-08-14T14:30:00"
      }
    ],
    "reviews": [
      {
        "reviewId": "review1",
        "storeName": "맛있는 식당",
        "comment": "정말 맛있어요!",
        "rating": 5,
        "createdAt": "2025-08-14T16:20:00"
      }
    ],
    "bookings": [
      {
        "bookingId": "booking1",
        "storeName": "맛있는 식당",
        "bookingDate": "2025-08-14T18:00:00",
        "status": "ACTIVE"
      }
    ]
  }
}
```

## 📊 **DB 팀이 수행해야 할 작업**

### **1. 테이블 구조 변경**

#### **FAV_STORE 테이블 수정**
```sql
-- STORE_NAME 컬럼 추가 (이미 추가됨)
ALTER TABLE FAV_STORE ADD COLUMN STORE_NAME VARCHAR(100);

-- 기존 데이터 업데이트 (Store Service와 연동 필요)
UPDATE FAV_STORE fs 
SET STORE_NAME = (
    SELECT s.STORE_NAME 
    FROM STORE s 
    WHERE s.STORE_ID = fs.STORE_ID2
);
```

#### **REVIEW 테이블 수정**
```sql
-- STORE_NAME 컬럼 추가
ALTER TABLE REVIEW ADD COLUMN STORE_NAME VARCHAR(100);

-- CREATED_AT 컬럼 추가 (DB 팀 피드백에 따라)
ALTER TABLE REVIEW ADD COLUMN CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- 기존 데이터 업데이트
UPDATE REVIEW r 
SET STORE_NAME = (
    SELECT s.STORE_NAME 
    FROM STORE s 
    WHERE s.STORE_ID = r.STORE_ID
);
```

### **2. 새로운 테이블 생성**

#### **BOOKING_STATE 테이블 생성**
```sql
CREATE TABLE BOOKING_STATE (
    BOOKING_STATE_CODE VARCHAR(20) PRIMARY KEY,
    BOOKING_STATE_NAME VARCHAR(50) NOT NULL,
    DESCRIPTION VARCHAR(200),
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 기본 상태 데이터 삽입
INSERT INTO BOOKING_STATE (BOOKING_STATE_CODE, BOOKING_STATE_NAME, DESCRIPTION) VALUES
('ACTIVE', '활성', '현재 유효한 예약'),
('CANCELLED', '취소됨', '사용자가 취소한 예약'),
('COMPLETED', '완료됨', '방문이 완료된 예약'),
('EXPIRED', '만료됨', '시간이 지나 만료된 예약');
```

### **3. VIEW 생성**

#### **V_USER_BOOKINGS 뷰 생성**
```sql
CREATE VIEW V_USER_BOOKINGS AS
SELECT 
    b.BOOKING_ID,
    b.USER_ID,
    b.STORE_ID,
    s.STORE_NAME,
    b.BOOKING_DATE,
    b.BOOKING_STATE_CODE,
    bs.BOOKING_STATE_NAME,
    b.CREATED_AT
FROM BOOKING b
JOIN STORE s ON b.STORE_ID = s.STORE_ID
JOIN BOOKING_STATE bs ON b.BOOKING_STATE_CODE = bs.BOOKING_STATE_CODE
WHERE b.USER_ID IS NOT NULL;
```

#### **V_USER_DASHBOARD 뷰 생성**
```sql
CREATE VIEW V_USER_DASHBOARD AS
SELECT 
    u.USER_ID,
    COALESCE(fav_count.COUNT, 0) AS TOTAL_FAVORITES,
    COALESCE(review_count.COUNT, 0) AS TOTAL_REVIEWS,
    COALESCE(booking_count.COUNT, 0) AS TOTAL_BOOKINGS,
    COALESCE(active_booking_count.COUNT, 0) AS ACTIVE_BOOKINGS
FROM USERS u
LEFT JOIN (
    SELECT USER_ID, COUNT(*) AS COUNT 
    FROM FAV_STORE 
    GROUP BY USER_ID
) fav_count ON u.USER_ID = fav_count.USER_ID
LEFT JOIN (
    SELECT USER_ID, COUNT(*) AS COUNT 
    FROM REVIEW 
    GROUP BY USER_ID
) review_count ON u.USER_ID = review_count.USER_ID
LEFT JOIN (
    SELECT USER_ID, COUNT(*) AS COUNT 
    FROM BOOKING 
    GROUP BY USER_ID
) booking_count ON u.USER_ID = booking_count.USER_ID
LEFT JOIN (
    SELECT USER_ID, COUNT(*) AS COUNT 
    FROM BOOKING 
    WHERE BOOKING_STATE_CODE = 'ACTIVE'
    GROUP BY USER_ID
) active_booking_count ON u.USER_ID = active_booking_count.USER_ID;
```

### **4. 권한 설정**

#### **USER_SERVICE 스키마에 권한 부여**
```sql
-- USER_SERVICE 스키마에 다른 서비스 테이블 조회 권한 부여
GRANT SELECT ON STORE TO user_service;
GRANT SELECT ON REVIEW TO user_service;
GRANT SELECT ON BOOKING TO user_service;
GRANT SELECT ON BOOKING_STATE TO user_service;

-- 뷰 조회 권한 부여
GRANT SELECT ON V_USER_BOOKINGS TO user_service;
GRANT SELECT ON V_USER_DASHBOARD TO user_service;
```

## 🔄 **기존 API 호환성**

### **Deprecated API들**
기존 API들은 `@Deprecated`로 표시되어 하위 호환성을 유지합니다:

```java
@GetMapping("/me/favorites/count")      // @Deprecated
@GetMapping("/me/reviews/details")      // @Deprecated
@GetMapping("/me/bookings/details")     // @Deprecated
@GetMapping("/me/dashboard/stats")      // @Deprecated
```

### **권장사항**
- **새로운 개발**: 통합 마이페이지 API (`/api/users/me`) 사용
- **기존 코드**: 점진적으로 통합 마이페이지 API로 마이그레이션

## 📱 **프론트엔드 구현 예시**

### **통합 마이페이지 컴포넌트**
```vue
<template>
  <div class="my-page">
    <!-- 사용자 정보 섹션 -->
    <div class="user-info-section">
      <h2>{{ userInfo.userName }}님의 마이페이지</h2>
      <div class="user-details">
        <p><strong>전화번호:</strong> {{ userInfo.phoneNumber }}</p>
        <p><strong>주소:</strong> {{ userInfo.userLocation }}</p>
        <p><strong>가입일:</strong> {{ formatDate(userInfo.createdAt) }}</p>
      </div>
    </div>
    
    <!-- 통계 섹션 -->
    <div class="statistics-section">
      <h3>내 활동 현황</h3>
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-number">{{ statistics.favoriteCount }}</div>
          <div class="stat-label">즐겨찾기</div>
        </div>
        <div class="stat-card">
          <div class="stat-number">{{ statistics.reviewCount }}</div>
          <div class="stat-label">리뷰</div>
        </div>
        <div class="stat-card">
          <div class="stat-number">{{ statistics.totalBookingCount }}</div>
          <div class="stat-label">전체 예약</div>
        </div>
        <div class="stat-card">
          <div class="stat-number">{{ statistics.activeBookingCount }}</div>
          <div class="stat-label">활성 예약</div>
        </div>
      </div>
    </div>
    
    <!-- 최근 활동 섹션 -->
    <div class="recent-activities">
      <h3>최근 활동</h3>
      
      <!-- 최근 즐겨찾기 -->
      <div class="activity-section">
        <h4>최근 즐겨찾기</h4>
        <div class="activity-list">
          <div v-for="fav in recentActivities.favorites" :key="fav.storeId" class="activity-item">
            <span class="activity-title">{{ fav.storeName }}</span>
            <span class="activity-date">{{ formatDate(fav.createdAt) }}</span>
          </div>
        </div>
      </div>
      
      <!-- 최근 리뷰 -->
      <div class="activity-section">
        <h4>최근 리뷰</h4>
        <div class="activity-list">
          <div v-for="review in recentActivities.reviews" :key="review.reviewId" class="activity-item">
            <span class="activity-title">{{ review.storeName }}</span>
            <span class="review-rating">⭐ {{ review.rating }}</span>
            <span class="activity-date">{{ formatDate(review.createdAt) }}</span>
          </div>
        </div>
      </div>
      
      <!-- 최근 예약 -->
      <div class="activity-section">
        <h4>최근 예약</h4>
        <div class="activity-list">
          <div v-for="booking in recentActivities.bookings" :key="booking.bookingId" class="activity-item">
            <span class="activity-title">{{ booking.storeName }}</span>
            <span class="booking-status" :class="booking.status">{{ booking.status }}</span>
            <span class="activity-date">{{ formatDate(booking.bookingDate) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      userInfo: {},
      statistics: {},
      recentActivities: {}
    }
  },
  
  async mounted() {
    await this.loadMyPage();
  },
  
  methods: {
    async loadMyPage() {
      try {
        const response = await this.$axios.get('/api/users/me');
        this.userInfo = response.data.userInfo;
        this.statistics = response.data.statistics;
        this.recentActivities = response.data.recentActivities;
      } catch (error) {
        console.error('마이페이지 로드 실패:', error);
      }
    },
    
    formatDate(dateString) {
      return new Date(dateString).toLocaleDateString('ko-KR');
    }
  }
}
</script>

## 🎯 **구현의 장점**

### **1. 사용자 경험 향상**
- ✅ **원스톱 접근**: 한 페이지에서 모든 정보 확인
- ✅ **빠른 로딩**: 한 번의 API 호출로 모든 정보 획득
- ✅ **직관적 UI**: 통계와 상세 정보를 함께 표시

### **2. 개발 효율성**
- ✅ **API 통합**: 중복 API 제거로 코드 간소화
- ✅ **성능 최적화**: 네트워크 요청 횟수 감소
- ✅ **유지보수성**: 통합된 구조로 관리 용이

### **3. 데이터베이스 최적화**
- ✅ **VIEW 사용**: Materialized View 대신 일반 View로 리소스 절약
- ✅ **컬럼 중복**: JOIN 제거로 쿼리 성능 향상
- ✅ **실시간 데이터**: 최신 정보 즉시 반영

## 📋 **DB 팀 작업 체크리스트**

### **Phase 1: 테이블 구조 변경**
- [ ] `FAV_STORE` 테이블에 `STORE_NAME` 컬럼 추가 확인
- [ ] `REVIEW` 테이블에 `STORE_NAME` 컬럼 추가
- [ ] `REVIEW` 테이블에 `CREATED_AT` 컬럼 추가
- [ ] 기존 데이터 업데이트 (Store Service와 연동)

### **Phase 2: 새로운 테이블 생성**
- [ ] `BOOKING_STATE` 테이블 생성
- [ ] 기본 상태 데이터 삽입

### **Phase 3: VIEW 생성**
- [ ] `V_USER_BOOKINGS` 뷰 생성
- [ ] `V_USER_DASHBOARD` 뷰 생성

### **Phase 4: 권한 설정**
- [ ] `user_service` 스키마에 필요한 권한 부여
- [ ] 뷰 조회 권한 부여

### **Phase 5: 테스트**
- [ ] 통합 마이페이지 API 테스트
- [ ] 데이터 정합성 확인
- [ ] 성능 테스트

## 🚀 **결론**

DB 담당 팀원의 피드백을 바탕으로 **대시보드 개념을 완전히 제거하고 마이페이지를 통합된 개인 정보 센터**로 성공적으로 개선했습니다. 

### **주요 성과**
1. **사용자 경험 개선**: 한 곳에서 모든 개인 정보 확인 가능
2. **성능 최적화**: VIEW 사용과 컬럼 중복으로 DB 부하 감소
3. **개발 효율성**: API 통합으로 코드 간소화
4. **확장성**: 모듈화된 구조로 향후 기능 추가 용이

이제 사용자가 훨씬 편리하게 자신의 정보를 확인할 수 있으며, DB 팀의 작업 완료 후 완전한 통합 마이페이지가 제공될 예정입니다! 🎉

---

**작성일**: 2024년 1월 15일  
**작성자**: User Service 개발팀  
**검토자**: DB 담당팀

## 🏗️ **MSA 원칙에 따른 서비스 역할 분담**

### **User Service의 역할**
- ✅ **사용자 정보 관리**: 회원가입, 로그인, 사용자 정보 수정
- ✅ **즐겨찾기 관리**: 사용자의 즐겨찾기 가게 추가/삭제/조회
- ✅ **리뷰 조회**: 사용자가 작성한 리뷰 목록 조회 (읽기 전용)
- ✅ **예약 조회**: 사용자의 예약 현황 조회 (읽기 전용)
- ✅ **통합 마이페이지**: 사용자 정보 + 통계 + 최근 활동 제공

### **Store Service의 역할**
- ✅ **가게 정보 관리**: 가게 등록, 수정, 삭제, 조회
- ✅ **리뷰 CRUD**: 리뷰 생성, 수정, 삭제, 조회
- ✅ **가게별 리뷰 관리**: 특정 가게의 리뷰 목록 관리

### **Reservation Service의 역할**
- ✅ **예약 관리**: 예약 생성, 수정, 삭제, 조회
- ✅ **대기열 관리**: 예약 대기열 처리
- ✅ **예약 상태 관리**: 예약 상태 변경 및 추적

## 📋 **리뷰 관리 구조 확인**

### **현재 구현 상태**
- ✅ **리뷰 조회**: User Service에서 Store Service API 호출하여 조회
- ✅ **가게 정보 조회**: 리뷰와 관련된 가게 정보 조회
- ❌ **리뷰 수정**: Store Service에서 담당 (User Service에서는 수정 불가)
- ❌ **리뷰 삭제**: Store Service에서 담당 (User Service에서는 삭제 불가)

### **MSA 원칙 준수**
현재 구조는 **MSA 원칙에 완전히 부합**합니다:

1. **서비스 경계 명확**: 각 서비스가 명확한 책임을 가짐
2. **데이터 소유권**: 리뷰 데이터는 Store Service가 소유
3. **API 호출**: User Service는 Store Service API를 호출하여 데이터 조회
4. **수정 권한**: 리뷰 수정/삭제는 Store Service에서만 가능

### **프론트엔드 연동 방식**
```javascript
// 리뷰 조회 (User Service)
const reviews = await axios.get('/api/users/me/reviews');

// 리뷰 수정 (Store Service로 직접 호출)
const updateReview = async (reviewId, reviewData) => {
  const response = await axios.put(`/api/stores/reviews/${reviewId}`, reviewData);
  return response.data;
};

// 리뷰 삭제 (Store Service로 직접 호출)
const deleteReview = async (reviewId) => {
  const response = await axios.delete(`/api/stores/reviews/${reviewId}`);
  return response.data;
};
```

## 📊 데이터베이스 뷰 최적화 (DB 팀 피드백 반영)

### 🔍 **문제점 분석**
기존 `V_USER_DASHBOARD` 뷰는 한 번에 4개의 서로 다른 스키마 테이블을 JOIN하여 성능 문제가 발생할 수 있습니다:
- 불필요한 NULL 값 조회
- 복잡한 JOIN으로 인한 성능 저하
- 특정 통계만 필요한 경우에도 전체 뷰를 조회해야 함

### ✅ **해결 방안: 개별 뷰로 분리**

#### 1. 즐겨찾기 개수 뷰
```sql
CREATE VIEW V_USER_FAVORITE_COUNT AS
SELECT 
    u.USER_ID,
    COALESCE(fav_count.COUNT, 0) AS TOTAL_FAVORITES
FROM USERS u
LEFT JOIN (
    SELECT USER_ID, COUNT(*) AS COUNT 
    FROM STORE_ADMIN.FAV_STORE@DATALINK_STORE
    GROUP BY USER_ID
) fav_count ON u.USER_ID = fav_count.USER_ID;
```

#### 2. 리뷰 개수 뷰
```sql
CREATE VIEW V_USER_REVIEW_COUNT AS
SELECT 
    u.USER_ID,
    COALESCE(review_count.COUNT, 0) AS TOTAL_REVIEWS
FROM USERS u
LEFT JOIN (
    SELECT USER_ID, COUNT(*) AS COUNT 
    FROM STORE_ADMIN.REVIEW@DATALINK_STORE
    GROUP BY USER_ID
) review_count ON u.USER_ID = review_count.USER_ID;
```

#### 3. 전체 예약 개수 뷰
```sql
CREATE VIEW V_USER_BOOKING_COUNT AS
SELECT 
    u.USER_ID,
    COALESCE(booking_count.COUNT, 0) AS TOTAL_BOOKINGS
FROM USERS u
LEFT JOIN (
    SELECT USER_ID, COUNT(*) AS COUNT 
    FROM BOOKING_ADMIN.BOOKING@DATALINK_BOOKING
    GROUP BY USER_ID
) booking_count ON u.USER_ID = booking_count.USER_ID;
```

#### 4. 활성 예약 개수 뷰
```sql
CREATE VIEW V_USER_ACTIVE_BOOKING_COUNT AS
SELECT 
    u.USER_ID,
    COALESCE(active_booking_count.COUNT, 0) AS ACTIVE_BOOKINGS
FROM USERS u
LEFT JOIN (
    SELECT b.USER_ID, COUNT(*) AS COUNT 
    FROM BOOKING_ADMIN.BOOKING@DATALINK_BOOKING b
    JOIN BOOKING_ADMIN.BOOKING_STATE_CODE@DATALINK_BOOKING bsc 
        ON b.BOOKING_STATE_CODE = bsc.BOOKING_STATE_CODE
    WHERE bsc.BOOKING_STATE_CODE = 'ACTIVE'
    GROUP BY b.USER_ID
) active_booking_count ON u.USER_ID = active_booking_count.USER_ID;
```

### 🔄 **User Service 코드 수정**

#### UserService.java 수정
```java
@Service
public class UserService {
    
    // 개별 통계 조회 메서드들
    public int getUserFavoriteCount(String userId) {
        try {
            return favoriteStoreRepository.getUserFavoriteCount(userId);
        } catch (Exception e) {
            logger.error("즐겨찾기 개수 조회 실패: userId={}, error={}", userId, e.getMessage());
            return 0;
        }
    }
    
    public int getUserReviewCount(String userId) {
        try {
            return favoriteStoreRepository.getUserReviewCount(userId);
        } catch (Exception e) {
            logger.error("리뷰 개수 조회 실패: userId={}, error={}", userId, e.getMessage());
            return 0;
        }
    }
    
    public int getUserBookingCount(String userId) {
        try {
            return favoriteStoreRepository.getUserBookingCount(userId);
        } catch (Exception e) {
            logger.error("예약 개수 조회 실패: userId={}, error={}", userId, e.getMessage());
            return 0;
        }
    }
    
    public int getActiveBookingCount(String userId) {
        try {
            return favoriteStoreRepository.getActiveBookingCount(userId);
        } catch (Exception e) {
            logger.error("활성 예약 개수 조회 실패: userId={}, error={}", userId, e.getMessage());
            return 0;
        }
    }
    
    // 통합 마이페이지 메서드 수정
    @Transactional(readOnly = true)
    public Map<String, Object> getMyPage(String userId) {
        logger.info("통합 마이페이지 정보 조회: userId={}", userId);

        Map<String, Object> myPage = new HashMap<>();

        try {
            // 1. 사용자 기본 정보
            User user = userRepository.findById(userId).orElse(null);
            Map<String, Object> userInfo = new HashMap<>();
            if (user != null) {
                userInfo.put("userId", user.getUserId());
                userInfo.put("userName", user.getUserName());
                userInfo.put("phoneNumber", user.getPhoneNumber());
                userInfo.put("userLocation", user.getUserLocation());
                userInfo.put("isActive", user.isActive());
                userInfo.put("createdAt", user.getCreatedAt());
                userInfo.put("updatedAt", user.getUpdatedAt());
            }
            myPage.put("userInfo", userInfo);

            // 2. 통계 정보 (개별 뷰 사용)
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("favoriteCount", getUserFavoriteCount(userId));
            statistics.put("reviewCount", getUserReviewCount(userId));
            statistics.put("totalBookingCount", getUserBookingCount(userId));
            statistics.put("activeBookingCount", getActiveBookingCount(userId));
            myPage.put("statistics", statistics);

            // 3. 최근 활동 (최근 5개씩)
            Map<String, Object> recentActivities = new HashMap<>();
            recentActivities.put("favorites", getRecentFavorites(userId, 5));
            recentActivities.put("reviews", getRecentReviews(userId, 5));
            recentActivities.put("bookings", getRecentBookings(userId, 5));
            myPage.put("recentActivities", recentActivities);

            logger.info("통합 마이페이지 정보 조회 완료: userId={}", userId);
            return myPage;

        } catch (Exception e) {
            logger.error("통합 마이페이지 정보 조회 중 오류 발생: userId={}, error={}", userId, e.getMessage());
            return getMyPageFallback(userId);
        }
    }
}
```

#### FavoriteStoreRepository.java 수정
```java
@Repository
public interface FavoriteStoreRepository extends JpaRepository<FavoriteStore, Long> {
    
    // 개별 통계 조회 쿼리들
    @Query(value = "SELECT TOTAL_FAVORITES FROM V_USER_FAVORITE_COUNT WHERE USER_ID = :userId", nativeQuery = true)
    int getUserFavoriteCount(@Param("userId") String userId);
    
    @Query(value = "SELECT TOTAL_REVIEWS FROM V_USER_REVIEW_COUNT WHERE USER_ID = :userId", nativeQuery = true)
    int getUserReviewCount(@Param("userId") String userId);
    
    @Query(value = "SELECT TOTAL_BOOKINGS FROM V_USER_BOOKING_COUNT WHERE USER_ID = :userId", nativeQuery = true)
    int getUserBookingCount(@Param("userId") String userId);
    
    @Query(value = "SELECT ACTIVE_BOOKINGS FROM V_USER_ACTIVE_BOOKING_COUNT WHERE USER_ID = :userId", nativeQuery = true)
    int getActiveBookingCount(@Param("userId") String userId);
    
    // 기존 메서드들...
}
```

### 📊 **성능 최적화 효과**

#### 1. **선택적 조회 가능**
```java
// 필요한 통계만 조회
int favoriteCount = userService.getUserFavoriteCount(userId);
int reviewCount = userService.getUserReviewCount(userId);
```

#### 2. **캐싱 적용 가능**
```java
@Cacheable(value = "userStats", key = "#userId + '_favorites'")
public int getUserFavoriteCount(String userId) {
    return favoriteStoreRepository.getUserFavoriteCount(userId);
}

@Cacheable(value = "userStats", key = "#userId + '_reviews'")
public int getUserReviewCount(String userId) {
    return favoriteStoreRepository.getUserReviewCount(userId);
}
```

#### 3. **에러 격리**
- 하나의 뷰에서 오류가 발생해도 다른 통계는 정상 조회 가능
- 각 통계별로 개별적인 에러 처리 가능

### 🔧 **DB 팀 작업 체크리스트**

#### 1. 기존 뷰 삭제
```sql
-- 기존 복합 뷰 삭제
DROP VIEW V_USER_DASHBOARD;
```

#### 2. 새로운 개별 뷰 생성
```sql
-- 1. 즐겨찾기 개수 뷰
CREATE VIEW V_USER_FAVORITE_COUNT AS
SELECT 
    u.USER_ID,
    COALESCE(fav_count.COUNT, 0) AS TOTAL_FAVORITES
FROM USERS u
LEFT JOIN (
    SELECT USER_ID, COUNT(*) AS COUNT 
    FROM STORE_ADMIN.FAV_STORE@DATALINK_STORE
    GROUP BY USER_ID
) fav_count ON u.USER_ID = fav_count.USER_ID;

-- 2. 리뷰 개수 뷰
CREATE VIEW V_USER_REVIEW_COUNT AS
SELECT 
    u.USER_ID,
    COALESCE(review_count.COUNT, 0) AS TOTAL_REVIEWS
FROM USERS u
LEFT JOIN (
    SELECT USER_ID, COUNT(*) AS COUNT 
    FROM STORE_ADMIN.REVIEW@DATALINK_STORE
    GROUP BY USER_ID
) review_count ON u.USER_ID = review_count.USER_ID;

-- 3. 전체 예약 개수 뷰
CREATE VIEW V_USER_BOOKING_COUNT AS
SELECT 
    u.USER_ID,
    COALESCE(booking_count.COUNT, 0) AS TOTAL_BOOKINGS
FROM USERS u
LEFT JOIN (
    SELECT USER_ID, COUNT(*) AS COUNT 
    FROM BOOKING_ADMIN.BOOKING@DATALINK_BOOKING
    GROUP BY USER_ID
) booking_count ON u.USER_ID = booking_count.USER_ID;

-- 4. 활성 예약 개수 뷰
CREATE VIEW V_USER_ACTIVE_BOOKING_COUNT AS
SELECT 
    u.USER_ID,
    COALESCE(active_booking_count.COUNT, 0) AS ACTIVE_BOOKINGS
FROM USERS u
LEFT JOIN (
    SELECT b.USER_ID, COUNT(*) AS COUNT 
    FROM BOOKING_ADMIN.BOOKING@DATALINK_BOOKING b
    JOIN BOOKING_ADMIN.BOOKING_STATE_CODE@DATALINK_BOOKING bsc 
        ON b.BOOKING_STATE_CODE = bsc.BOOKING_STATE_CODE
    WHERE bsc.BOOKING_STATE_CODE = 'ACTIVE'
    GROUP BY b.USER_ID
) active_booking_count ON u.USER_ID = active_booking_count.USER_ID;
```

#### 3. 권한 설정
```sql
-- user_service 스키마에 SELECT 권한 부여
GRANT SELECT ON V_USER_FAVORITE_COUNT TO user_service;
GRANT SELECT ON V_USER_REVIEW_COUNT TO user_service;
GRANT SELECT ON V_USER_BOOKING_COUNT TO user_service;
GRANT SELECT ON V_USER_ACTIVE_BOOKING_COUNT TO user_service;
```

### 📈 **성능 개선 효과**

#### 1. **쿼리 실행 시간 단축**
- 복합 JOIN → 단일 JOIN으로 변경
- 필요한 통계만 선택적 조회

#### 2. **리소스 사용량 감소**
- 불필요한 NULL 값 조회 제거
- 메모리 사용량 최적화

#### 3. **확장성 향상**
- 새로운 통계 추가 시 기존 뷰에 영향 없음
- 각 통계별 독립적인 최적화 가능

### 🎯 **결론**

DB 팀의 피드백을 반영하여 **복합 뷰를 개별 뷰로 분리**함으로써:
- ✅ **성능 최적화**: 불필요한 JOIN 제거
- ✅ **유연성 향상**: 선택적 통계 조회 가능
- ✅ **안정성 개선**: 에러 격리 및 개별 처리
- ✅ **확장성**: 새로운 통계 추가 용이

이제 User Service에서 필요한 통계만 효율적으로 조회할 수 있습니다!

