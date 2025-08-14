---

**문서 버전**: 2.0  
**최종 업데이트**: 2025년 8월 14일  
**작성자**: Team-FOG User Service 개발팀

# 📚 User Service API 문서

## 📋 개요

Team-FOG User Service의 모든 API 엔드포인트에 대한 상세한 문서입니다.

**Base URL**: `http://localhost:8082` (개발) / `https://api.team-fog.com` (프로덕션)

## 🔐 인증

### JWT 토큰 인증

대부분의 API는 JWT 토큰 인증이 필요합니다. 토큰은 HTTP 헤더에 포함해야 합니다:

```
Authorization: Bearer YOUR_JWT_TOKEN
```

### 토큰 획득 방법

1. **더미 로그인 (개발용)**:
   ```bash
   GET /api/users/login/dummy?state=test-state
   ```

2. **실제 Cognito 로그인 (프로덕션)**:
   ```bash
   GET /api/users/login/url
   POST /api/users/login/callback
   ```

## 🔌 API 엔드포인트

### 1. 인증 관련 API

#### 1.1 Cognito 로그인 URL 생성

**엔드포인트**: `GET /api/users/login/url`

**설명**: AWS Cognito 로그인 페이지 URL을 생성합니다.

**인증 필요**: ❌

**응답 예시**:
```json
{
  "url": "https://team-fog.auth.ap-northeast-2.amazoncognito.com/oauth2/authorize?response_type=code&client_id=xxxxxxxxx&redirect_uri=http://localhost:3000/callback&scope=openid+email+profile&state=uuid-here",
  "state": "uuid-here"
}
```

#### 1.2 더미 로그인 (개발용)

**엔드포인트**: `GET /api/users/login/dummy`

**설명**: 개발 및 테스트를 위한 더미 로그인을 수행합니다.

**인증 필요**: ❌

**쿼리 파라미터**:
- `state` (필수): 상태 값

**응답 예시**:
```json
{
  "success": true,
  "accessToken": "dummy-access-token-1234567890",
  "idToken": "dummy-id-token-1234567890",
  "refreshToken": "dummy-refresh-token-1234567890",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userInfo": {
    "sub": "dummy-user-123",
    "username": "더미사용자",
    "email": "dummy@example.com",
    "name": "더미사용자"
  },
  "message": "더미 로그인 성공"
}
```

#### 1.3 Cognito 콜백 처리

**엔드포인트**: `POST /api/users/login/callback`

**설명**: Cognito 인증 코드를 토큰으로 교환합니다.

**인증 필요**: ❌

**요청 본문**:
```json
{
  "code": "authorization_code_from_cognito",
  "state": "state_value"
}
```

**응답 예시**:
```json
{
  "success": true,
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "idToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userInfo": {
    "sub": "user-uuid",
    "username": "사용자명",
    "email": "user@example.com"
  },
  "message": "Cognito 로그인 성공"
}
```

#### 1.4 로그아웃

**엔드포인트**: `POST /api/users/logout`

**설명**: 사용자 로그아웃을 처리합니다.

**인증 필요**: ✅

**응답 예시**:
```json
{
  "success": true,
  "message": "로그아웃 성공"
}
```

### 2. 사용자 관리 API

#### 2.1 회원가입

**엔드포인트**: `POST /api/users`

**설명**: 새로운 사용자를 등록합니다.

**인증 필요**: ❌

**요청 본문**:
```json
{
  "userId": "user123",
  "userName": "홍길동",
  "phoneNumber": "010-1234-5678",
  "userLocation": "서울시 강남구",
  "password": "password123"
}
```

**응답 예시**:
```json
{
  "userId": "user123",
  "userName": "홍길동",
  "phoneNumber": "010-1234-5678",
  "userLocation": "서울시 강남구",
  "createdAt": "2024-01-15T10:30:00"
}
```

#### 2.2 통합 마이페이지 조회

**엔드포인트**: `GET /api/users/me`

**설명**: 사용자 정보 + 통계 + 최근 활동을 한번에 제공합니다.

**인증 필요**: ✅

**응답 예시**:
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

#### 2.3 사용자 정보 수정

**엔드포인트**: `PUT /api/users/me`

**설명**: 사용자 정보를 수정합니다.

**인증 필요**: ✅

**요청 본문**:
```json
{
  "userName": "홍길동",
  "phoneNumber": "010-1234-5678",
  "userLocation": "서울시 강남구"
}
```

**응답 예시**:
```json
{
  "userId": "user123",
  "userName": "홍길동",
  "phoneNumber": "010-1234-5678",
  "userLocation": "서울시 강남구",
  "isActive": true,
  "createdAt": "2025-08-14T10:30:00",
  "updatedAt": "2025-08-14T11:30:00"
}
```

#### 2.4 전체 사용자 수 조회

**엔드포인트**: `GET /api/users/count`

**설명**: 전체 사용자 수를 조회합니다.

**인증 필요**: ❌

**응답 예시**:
```json
{
  "count": 150
}
```

### 3. 즐겨찾기 관리 API

#### 3.1 즐겨찾기 가게 목록 조회

**엔드포인트**: `GET /api/users/me/favorites`

**설명**: 사용자가 즐겨찾기한 가게 목록을 조회합니다.

**인증 필요**: ✅

**응답 예시**:
```json
[
  {
    "favStoreId": 1,
    "storeId": "store1",
    "storeName": "맛있는 식당",
    "userId": "user123",
    "createdAt": "2025-08-14T14:30:00"
  }
]
```

#### 3.2 즐겨찾기 가게 추가

**엔드포인트**: `POST /api/users/me/favorites`

**설명**: 가게를 즐겨찾기에 추가합니다.

**인증 필요**: ✅

**요청 본문**:
```json
{
  "storeId": "store1"
}
```

**응답 예시**:
```json
{
  "favStoreId": 1,
  "storeId": "store1",
  "storeName": "맛있는 식당",
  "userId": "user123",
  "createdAt": "2025-08-14T14:30:00"
}
```

#### 3.3 즐겨찾기 가게 삭제

**엔드포인트**: `DELETE /api/users/me/favorites/{storeId}`

**설명**: 즐겨찾기한 가게를 삭제합니다.

**인증 필요**: ✅

**경로 파라미터**:
- `storeId` (필수): 가게 ID

**응답 예시**:
```json
{
  "success": true,
  "message": "즐겨찾기 가게가 삭제되었습니다."
}
```

#### 3.4 즐겨찾기 상태 확인

**엔드포인트**: `GET /api/users/me/favorites/{storeId}/check`

**설명**: 특정 가게의 즐겨찾기 상태를 확인합니다.

**인증 필요**: ✅

**경로 파라미터**:
- `storeId` (필수): 가게 ID

**응답 예시**:
```json
{
  "isFavorite": true,
  "storeId": "store1"
}
```

### 4. 리뷰 관련 API

#### 4.1 내 리뷰 목록 조회

**엔드포인트**: `GET /api/users/me/reviews`

**설명**: 사용자가 작성한 리뷰 목록을 조회합니다.

**인증 필요**: ✅

**응답 예시**:
```json
[
  {
    "reviewId": 1,
    "storeId": "store1",
    "storeName": "맛있는 식당",
    "userId": "user123",
    "userName": "홍길동",
    "comment": "정말 맛있어요!",
    "rating": 5,
    "createdAt": "2025-08-14T16:20:00",
    "updatedAt": "2025-08-14T16:20:00"
  }
]
```

#### 4.2 리뷰 관련 가게 정보 조회

**엔드포인트**: `GET /api/users/me/reviews/{reviewId}/store-info`

**설명**: 특정 리뷰와 관련된 가게 정보를 조회합니다.

**인증 필요**: ✅

**경로 파라미터**:
- `reviewId` (필수): 리뷰 ID

**응답 예시**:
```json
{
  "storeId": "store1",
  "storeName": "맛있는 식당",
  "storeUrl": "/stores/store1/reviews"
}
```

### 5. 시스템 관리 API

#### 5.1 서비스 헬스체크

**엔드포인트**: `GET /api/users/health`

**설명**: User Service의 상태를 확인합니다.

**인증 필요**: ❌

**응답 예시**:
```json
{
  "service": "user-service",
  "status": "UP",
  "timestamp": 1705123456789
}
```

#### 5.2 더미 데이터 생성 (개발용)

**엔드포인트**: `POST /api/users/dummy/data`

**설명**: 개발 및 테스트를 위한 더미 데이터를 생성합니다.

**인증 필요**: ❌

**응답 예시**:
```json
{
  "success": false,
  "message": "더미 데이터 생성은 개발 환경에서만 사용 가능합니다."
}
```

### 6. Deprecated API (하위 호환성 유지)

#### 6.1 즐겨찾기 개수 조회 (Deprecated)

**엔드포인트**: `GET /api/users/me/favorites/count`

**설명**: 즐겨찾기 가게 개수를 조회합니다. (통합 마이페이지 API 사용 권장)

**인증 필요**: ✅

**응답 예시**:
```json
{
  "count": 5
}
```

#### 6.2 리뷰 상세 정보 조회 (Deprecated)

**엔드포인트**: `GET /api/users/me/reviews/details`

**설명**: 뷰를 사용한 리뷰 상세 정보를 조회합니다. (통합 마이페이지 API 사용 권장)

**인증 필요**: ✅

**응답 예시**:
```json
[
  {
    "storeName": "맛있는 식당",
    "comment": "정말 맛있어요!",
    "createdAt": "2025-08-14T16:20:00"
  }
]
```

#### 6.3 예약 상세 정보 조회 (Deprecated)

**엔드포인트**: `GET /api/users/me/bookings/details`

**설명**: 뷰를 사용한 예약 상세 정보를 조회합니다. (통합 마이페이지 API 사용 권장)

**인증 필요**: ✅

**응답 예시**:
```json
[
  {
    "storeName": "맛있는 식당",
    "bookingDate": "2025-08-14T18:00:00",
    "bookingStateName": "ACTIVE"
  }
]
```

#### 6.4 대시보드 통계 조회 (Deprecated)

**엔드포인트**: `GET /api/users/me/dashboard/stats`

**설명**: 뷰를 사용한 대시보드 통계를 조회합니다. (통합 마이페이지 API 사용 권장)

**인증 필요**: ✅

**응답 예시**:
```json
{
  "totalFavorites": 5,
  "totalReviews": 12,
  "totalBookings": 8,
  "activeBookings": 2
}
```

## 🔧 에러 응답

### 공통 에러 응답 형식

```json
{
  "success": false,
  "message": "에러 메시지",
  "errorCode": "ERROR_CODE",
  "timestamp": "2025-08-14T10:30:00",
  "data": null
}
```