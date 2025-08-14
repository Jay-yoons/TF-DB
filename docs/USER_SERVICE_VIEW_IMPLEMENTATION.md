# 🔧 User Service 뷰 처리 구현 가이드

## 📋 개요

DB 담당자가 뷰 처리와 JOIN 관련 변경을 언급한 경우, User Service에서 수정해야 할 부분들을 정리한 가이드입니다.

## 🚨 **즉시 DB 담당자와 협의해야 할 사항**

### **1. 뷰 생성 권한 확인**
```sql
-- User Service 사용자에게 필요한 권한들
GRANT CREATE VIEW TO user_service;
GRANT SELECT ON STORES TO user_service;
GRANT SELECT ON CATEGORY TO user_service;
GRANT SELECT ON REVIEW TO user_service;
GRANT SELECT ON BOOKING TO user_service;
GRANT SELECT ON BOOKING_STATE_CODE TO user_service;
GRANT SELECT ON STORE_SEAT TO user_service;
```

### **2. 뷰 생성 스크립트 실행**
```sql
-- DB 담당자가 실행해야 할 뷰 생성 스크립트
-- docs/USER_SERVICE_VIEWS.md 파일의 뷰 정의들을 실행
```

### **3. 뷰 테스트**
```sql
-- 뷰가 정상적으로 생성되었는지 테스트
SELECT * FROM V_USER_FAVORITE_STORES WHERE USER_ID = 'test_user';
SELECT * FROM V_USER_REVIEWS WHERE USER_ID = 'test_user';
SELECT * FROM V_USER_BOOKINGS WHERE USER_ID = 'test_user';
SELECT * FROM V_USER_DASHBOARD WHERE USER_ID = 'test_user';
```

## 🔄 **User Service에서 수정된 부분들**

### **1. Repository 수정**
- `FavoriteStoreRepository.java`에 뷰 조회 메서드 추가
- `@Query` 어노테이션으로 네이티브 쿼리 사용

### **2. Service 수정**
- `UserService.java`에 뷰 사용 메서드 추가
- Fallback 메서드로 기존 방식 유지
- 에러 처리 및 로깅 강화

### **3. Controller 수정**
- `UserController.java`에 새로운 API 엔드포인트 추가
- 기존 API와 새로운 API 모두 제공

## 📊 **새로운 API 엔드포인트들**

### **1. 뷰를 사용한 즐겨찾기 가게 상세 정보**
```
GET /api/users/me/favorites/details
```
- **기존**: `GET /api/users/me/favorites` (더미 데이터)
- **개선**: 가게 이름, 주소, 카테고리 등 상세 정보 포함

### **2. 뷰를 사용한 사용자 리뷰 상세 정보**
```
GET /api/users/me/reviews/details
```
- **기존**: Store Service API 호출
- **개선**: 뷰를 통한 직접 조회로 성능 향상

### **3. 뷰를 사용한 사용자 예약 현황**
```
GET /api/users/me/bookings/details
```
- **새로운**: 예약 상태, 가게 정보 등 상세 정보

### **4. 뷰를 사용한 사용자 대시보드 통계**
```
GET /api/users/me/dashboard/stats
```
- **기존**: `GET /api/users/dashboard/counts` (더미 데이터)
- **개선**: 실제 사용자 활동 통계

## ⚠️ **주의사항 및 체크리스트**

### **DB 담당자 협의 사항**
- [ ] 뷰 생성 권한 부여 완료
- [ ] 뷰 생성 스크립트 실행 완료
- [ ] 뷰 테스트 완료
- [ ] 성능 테스트 완료
- [ ] 데이터 일관성 확인

### **개발자 체크리스트**
- [ ] 새로운 API 엔드포인트 테스트
- [ ] Fallback 메서드 동작 확인
- [ ] 에러 처리 확인
- [ ] 로깅 확인
- [ ] 기존 API와의 호환성 확인

## 🔧 **테스트 방법**

### **1. 뷰 존재 여부 확인**
```bash
# User Service에서 뷰 조회 테스트
curl -X GET "http://localhost:8082/api/users/me/favorites/details" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### **2. Fallback 동작 확인**
```bash
# 뷰가 없을 때 기존 방식으로 동작하는지 확인
curl -X GET "http://localhost:8082/api/users/me/favorites" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### **3. 성능 비교**
```bash
# 기존 API vs 새로운 API 응답 시간 비교
time curl -X GET "http://localhost:8082/api/users/me/favorites"
time curl -X GET "http://localhost:8082/api/users/me/favorites/details"
```

## 📈 **예상 효과**

### **1. 성능 향상**
- 복잡한 JOIN 쿼리를 뷰로 최적화
- 네트워크 호출 감소 (MSA 간 API 호출 대신 뷰 사용)

### **2. 데이터 일관성**
- 실시간 데이터 조회
- 원본 테이블 변경 시 자동 반영

### **3. 코드 단순화**
- 복잡한 API 호출 로직 제거
- 단순한 SELECT 쿼리로 변경

## 🚀 **배포 전 확인사항**

### **1. DB 환경**
- [ ] 뷰가 정상적으로 생성되었는지 확인
- [ ] 권한이 올바르게 부여되었는지 확인
- [ ] 테스트 데이터로 뷰 동작 확인

### **2. 애플리케이션 환경**
- [ ] 새로운 API 엔드포인트 정상 동작 확인
- [ ] Fallback 메서드 정상 동작 확인
- [ ] 로그 정상 출력 확인

### **3. 성능 테스트**
- [ ] 뷰 조회 성능 측정
- [ ] 동시 접속 테스트
- [ ] 메모리 사용량 확인

## 📞 **DB 담당자 연락사항**

### **필요한 정보**
1. 뷰 생성 완료 여부
2. 뷰 테스트 결과
3. 성능 테스트 결과
4. 권한 설정 완료 여부

### **협의 사항**
1. 뷰 스키마 변경 시 알림 방법
2. 뷰 성능 모니터링 방법
3. 뷰 백업 및 복구 방법
4. 뷰 권한 관리 방법
