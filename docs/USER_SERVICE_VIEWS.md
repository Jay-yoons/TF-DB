# 👤 User Service Database Views

## 📋 개요

User Service에서 사용할 Oracle Database View들을 정의합니다.
이 뷰들은 User Service가 다른 서비스의 데이터에 접근할 때 사용됩니다.

## 🏗️ 뷰 정의

### 1. **사용자 즐겨찾기 가게 상세 정보 뷰**
```sql
-- 사용자의 즐겨찾기 가게와 가게 상세 정보를 조인한 뷰
CREATE OR REPLACE VIEW V_USER_FAVORITE_STORES AS
SELECT 
    f.FAV_STORE_ID,
    f.USER_ID,
    f.STORE_ID2 AS STORE_ID,
    s.STORE_NAME,
    s.STORE_LOCATION,
    s.SERVICE_TIME,
    s.CATEGORY_CODE,
    c.CATEGORY_NAME,
    s.SEAT_NUM,
    f.CREATED_AT AS FAVORITE_CREATED_AT
FROM FAV_STORE f
JOIN STORES s ON f.STORE_ID2 = s.STORE_ID
JOIN CATEGORY c ON s.CATEGORY_CODE = c.CATEGORY_CODE
WHERE f.USER_ID IS NOT NULL;

-- 뷰 사용 예시
SELECT * FROM V_USER_FAVORITE_STORES WHERE USER_ID = 'user123';
```

### 2. **사용자 리뷰 상세 정보 뷰**
```sql
-- 사용자가 작성한 리뷰와 가게 정보를 조인한 뷰
CREATE OR REPLACE VIEW V_USER_REVIEWS AS
SELECT 
    r.REVIEW_ID,
    r.USER_ID,
    r.STORE_ID,
    s.STORE_NAME,
    s.STORE_LOCATION,
    s.CATEGORY_CODE,
    c.CATEGORY_NAME,
    r.COMMENT,
    r.SCORE,
    r.REVIEW_ID AS REVIEW_ID_STR
FROM REVIEW r
JOIN STORES s ON r.STORE_ID = s.STORE_ID
JOIN CATEGORY c ON s.CATEGORY_CODE = c.CATEGORY_CODE
WHERE r.USER_ID IS NOT NULL;

-- 뷰 사용 예시
SELECT * FROM V_USER_REVIEWS WHERE USER_ID = 'user123';
```

### 3. **사용자 예약 현황 뷰**
```sql
-- 사용자의 예약 정보와 가게 정보를 조인한 뷰
CREATE OR REPLACE VIEW V_USER_BOOKINGS AS
SELECT 
    b.BOOKING_NUM,
    b.USER_ID,
    b.STORE_ID,
    s.STORE_NAME,
    s.STORE_LOCATION,
    s.CATEGORY_CODE,
    c.CATEGORY_NAME,
    b.BOOKING_DATE,
    b.BOOKING_STATE_CODE,
    bsc.STATE_NAME,
    b.COUNT,
    ss.IN_USING_SEAT,
    s.SEAT_NUM
FROM BOOKING b
JOIN STORES s ON b.STORE_ID = s.STORE_ID
JOIN CATEGORY c ON s.CATEGORY_CODE = c.CATEGORY_CODE
JOIN BOOKING_STATE_CODE bsc ON b.BOOKING_STATE_CODE = bsc.BOOKING_STATE_CODE
LEFT JOIN STORE_SEAT ss ON b.STORE_ID = ss.STORE_ID
WHERE b.USER_ID IS NOT NULL;

-- 뷰 사용 예시
SELECT * FROM V_USER_BOOKINGS WHERE USER_ID = 'user123';
```

### 4. **사용자 대시보드 통계 뷰**
```sql
-- 사용자의 활동 통계를 제공하는 뷰
CREATE OR REPLACE VIEW V_USER_DASHBOARD AS
SELECT 
    u.USER_ID,
    u.USER_NAME,
    u.PHONE_NUMBER,
    u.USER_LOCATION,
    u.IS_ACTIVE,
    u.CREATED_AT AS USER_CREATED_AT,
    -- 즐겨찾기 통계
    COUNT(DISTINCT f.STORE_ID2) AS FAVORITE_COUNT,
    -- 리뷰 통계
    COUNT(DISTINCT r.REVIEW_ID) AS REVIEW_COUNT,
    AVG(r.SCORE) AS AVG_REVIEW_SCORE,
    -- 예약 통계
    COUNT(DISTINCT b.BOOKING_NUM) AS TOTAL_BOOKING_COUNT,
    COUNT(CASE WHEN b.BOOKING_STATE_CODE = 'RESERVED' THEN 1 END) AS ACTIVE_BOOKING_COUNT,
    COUNT(CASE WHEN b.BOOKING_STATE_CODE = 'CANCELLED' THEN 1 END) AS CANCELLED_BOOKING_COUNT
FROM USERS u
LEFT JOIN FAV_STORE f ON u.USER_ID = f.USER_ID
LEFT JOIN REVIEW r ON u.USER_ID = r.USER_ID
LEFT JOIN BOOKING b ON u.USER_ID = b.USER_ID
GROUP BY u.USER_ID, u.USER_NAME, u.PHONE_NUMBER, u.USER_LOCATION, u.IS_ACTIVE, u.CREATED_AT;

-- 뷰 사용 예시
SELECT * FROM V_USER_DASHBOARD WHERE USER_ID = 'user123';
```

### 5. **가게별 사용자 활동 뷰**
```sql
-- 특정 가게에서의 사용자 활동을 보여주는 뷰
CREATE OR REPLACE VIEW V_STORE_USER_ACTIVITY AS
SELECT 
    s.STORE_ID,
    s.STORE_NAME,
    s.STORE_LOCATION,
    s.CATEGORY_CODE,
    c.CATEGORY_NAME,
    -- 사용자 활동 통계
    COUNT(DISTINCT f.USER_ID) AS FAVORITE_USER_COUNT,
    COUNT(DISTINCT r.USER_ID) AS REVIEW_USER_COUNT,
    COUNT(DISTINCT b.USER_ID) AS BOOKING_USER_COUNT,
    AVG(r.SCORE) AS AVG_STORE_SCORE,
    COUNT(r.REVIEW_ID) AS TOTAL_REVIEW_COUNT
FROM STORES s
JOIN CATEGORY c ON s.CATEGORY_CODE = c.CATEGORY_CODE
LEFT JOIN FAV_STORE f ON s.STORE_ID = f.STORE_ID2
LEFT JOIN REVIEW r ON s.STORE_ID = r.STORE_ID
LEFT JOIN BOOKING b ON s.STORE_ID = b.STORE_ID
GROUP BY s.STORE_ID, s.STORE_NAME, s.STORE_LOCATION, s.CATEGORY_CODE, c.CATEGORY_NAME;

-- 뷰 사용 예시
SELECT * FROM V_STORE_USER_ACTIVITY WHERE STORE_ID = 'store001';
```

## 🔧 뷰 생성 스크립트

### User Service 사용자에게 뷰 생성 권한 부여
```sql
-- User Service 사용자에게 뷰 생성 권한 부여
GRANT CREATE VIEW TO user_service;
GRANT SELECT ON STORES TO user_service;
GRANT SELECT ON CATEGORY TO user_service;
GRANT SELECT ON REVIEW TO user_service;
GRANT SELECT ON BOOKING TO user_service;
GRANT SELECT ON BOOKING_STATE_CODE TO user_service;
GRANT SELECT ON STORE_SEAT TO user_service;
```

### 뷰 생성 실행 스크립트
```sql
-- User Service 스키마로 전환
ALTER SESSION SET CURRENT_SCHEMA = user_service;

-- 뷰들 생성
@user_service_views.sql
```

## 📊 뷰 사용 예시

### 1. **사용자 마이페이지 데이터 조회**
```java
// UserService.java
@Service
public class UserService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    // 사용자 대시보드 정보 조회
    public Map<String, Object> getUserDashboard(String userId) {
        String sql = "SELECT * FROM V_USER_DASHBOARD WHERE USER_ID = ?";
        return jdbcTemplate.queryForMap(sql, userId);
    }
    
    // 사용자 즐겨찾기 가게 목록 조회
    public List<Map<String, Object>> getUserFavoriteStores(String userId) {
        String sql = "SELECT * FROM V_USER_FAVORITE_STORES WHERE USER_ID = ? ORDER BY FAVORITE_CREATED_AT DESC";
        return jdbcTemplate.queryForList(sql, userId);
    }
    
    // 사용자 리뷰 목록 조회
    public List<Map<String, Object>> getUserReviews(String userId) {
        String sql = "SELECT * FROM V_USER_REVIEWS WHERE USER_ID = ? ORDER BY REVIEW_ID DESC";
        return jdbcTemplate.queryForList(sql, userId);
    }
}
```

### 2. **API 엔드포인트에서 뷰 활용**
```java
// UserController.java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/me/dashboard")
    public ResponseEntity<Map<String, Object>> getMyDashboard() {
        String userId = getCurrentUserId();
        Map<String, Object> dashboardData = userService.getUserDashboard(userId);
        return ResponseEntity.ok(dashboardData);
    }
    
    @GetMapping("/me/favorites")
    public ResponseEntity<List<Map<String, Object>>> getMyFavorites() {
        String userId = getCurrentUserId();
        List<Map<String, Object>> favorites = userService.getUserFavoriteStores(userId);
        return ResponseEntity.ok(favorites);
    }
}
```

## 🚀 뷰의 장점

### 1. **성능 최적화**
- 복잡한 조인 쿼리를 미리 정의하여 성능 향상
- 인덱스 활용으로 빠른 조회 가능

### 2. **코드 단순화**
- 복잡한 SQL을 뷰로 캡슐화
- Java 코드에서 간단한 SELECT 문만 사용

### 3. **데이터 일관성**
- 여러 테이블의 데이터를 일관된 형태로 제공
- 원본 데이터 변경 시 자동 반영

### 4. **보안 강화**
- 필요한 컬럼만 노출
- 사용자별 접근 권한 제어 가능

## ⚠️ 주의사항

1. **권한 관리**: 다른 서비스 테이블에 대한 SELECT 권한 필요
2. **성능 모니터링**: 뷰 조회 성능을 정기적으로 확인
3. **데이터 동기화**: MSA 환경에서 데이터 일관성 유지
4. **뷰 업데이트**: 스키마 변경 시 뷰도 함께 업데이트 필요
