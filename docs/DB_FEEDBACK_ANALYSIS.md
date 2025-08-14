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
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
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
        "createdAt": "2024-01-15T14:30:00"
      }
    ],
    "reviews": [
      {
        "reviewId": "review1",
        "storeName": "맛있는 식당",
        "comment": "정말 맛있어요!",
        "rating": 5,
        "createdAt": "2024-01-10T16:20:00"
      }
    ],
    "bookings": [
      {
        "bookingId": "booking1",
        "storeName": "맛있는 식당",
        "bookingDate": "2024-01-20T18:00:00",
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
```

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

