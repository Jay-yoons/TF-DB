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
  "loginUrl": "https://team-fog.auth.ap-northeast-2.amazoncognito.com/oauth2/authorize?response_type=code&client_id=xxxxxxxxx&redirect_uri=http://localhost:3000/callback&scope=openid+email+profile&state=uuid-here",
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
    "sub": "dummy4879",
    "name": "더미 사용자",
    "email": "dummy@example.com"
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
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "idToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJjdHkiOiJKV1QiLCJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiUlNBLU9BRVAifQ...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userInfo": {
    "sub": "user123",
    "name": "홍길동",
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
  "password": "securePassword123"
}
```

**응답 예시**:
```json
{
  "userId": "user123",
  "userName": "홍길동",
  "phoneNumber": "010-1234-5678",
  "userLocation": "서울시 강남구",
  "createdAt": "2025-08-12T10:14:10.085936"
}
```

**에러 응답**:
```json
{
  "error": "이미 등록된 사용자 ID입니다.",
  "timestamp": "2025-08-12T10:14:10.085936"
}
```

#### 2.2 내 정보 조회

**엔드포인트**: `GET /api/users/me`

**설명**: 현재 로그인한 사용자의 정보를 조회합니다.

**인증 필요**: ✅

**응답 예시**:
```json
{
  "userId": "user123",
  "userName": "홍길동",
  "phoneNumber": "010-1234-5678",
  "userLocation": "서울시 강남구",
  "createdAt": "2025-08-12T10:14:10.085936",
  "updatedAt": "2025-08-12T10:14:10.085936",
  "active": true
}
```

#### 2.3 사용자 정보 수정

**엔드포인트**: `PUT /api/users/me`

**설명**: 현재 로그인한 사용자의 정보를 수정합니다.

**인증 필요**: ✅

**요청 본문**:
```json
{
  "userName": "홍길순",
  "phoneNumber": "010-9876-5432",
  "userLocation": "서울시 서초구"
}
```

**응답 예시**:
```json
{
  "userId": "user123",
  "userName": "홍길순",
  "phoneNumber": "010-9876-5432",
  "userLocation": "서울시 서초구",
  "createdAt": "2025-08-12T10:14:10.085936",
  "updatedAt": "2025-08-12T10:15:30.123456",
  "active": true
}
```

#### 2.4 전체 사용자 수 조회

**엔드포인트**: `GET /api/users/count`

**설명**: 전체 등록된 사용자 수를 조회합니다.

**인증 필요**: ❌

**응답 예시**:
```json
{
  "count": 150
}
```

### 3. 마이페이지 API

#### 3.1 내가 작성한 리뷰 목록 조회

**엔드포인트**: `GET /api/users/me/reviews`

**설명**: 현재 사용자가 작성한 리뷰 목록을 조회합니다.

**인증 필요**: ✅

**응답 예시**:
```json
[
  {
    "reviewId": 1,
    "storeId": "store001",
    "storeName": "맛있는 한식당",
    "userId": "user123",
    "userName": "홍길동",
    "content": "정말 맛있었어요! 다음에 또 방문하고 싶습니다.",
    "rating": 5,
    "createdAt": "2025-08-10T10:14:36.8696669",
    "updatedAt": "2025-08-10T10:14:36.8696669"
  },
  {
    "reviewId": 2,
    "storeId": "store002",
    "storeName": "신선한 중식당",
    "userId": "user123",
    "userName": "홍길동",
    "content": "음식이 신선하고 맛있었습니다. 서비스도 좋았어요.",
    "rating": 4,
    "createdAt": "2025-08-07T10:14:36.8696669",
    "updatedAt": "2025-08-07T10:14:36.8696669"
  }
]
```

#### 3.2 리뷰 관련 가게 정보 조회

**엔드포인트**: `GET /api/users/me/reviews/{reviewId}/store-info`

**설명**: 특정 리뷰와 관련된 가게 정보를 조회합니다.

**인증 필요**: ✅

**경로 파라미터**:
- `reviewId` (필수): 리뷰 ID

**응답 예시**:
```json
{
  "storeId": "store001",
  "storeName": "맛있는 한식당",
  "storeUrl": "/stores/store001/reviews"
}
```

#### 3.3 내 즐겨찾기 가게 목록 조회

**엔드포인트**: `GET /api/users/me/favorites`

**설명**: 현재 사용자의 즐겨찾기 가게 목록을 조회합니다.

**인증 필요**: ✅

**응답 예시**:
```json
[
  {
    "favStoreId": 1,
    "storeId": "store001",
    "storeName": "맛있는 한식당",
    "userId": "user123",
    "createdAt": "2025-08-07T10:15:24.7718499"
  },
  {
    "favStoreId": 2,
    "storeId": "store002",
    "storeName": "신선한 중식당",
    "userId": "user123",
    "createdAt": "2025-08-09T10:15:24.7718499"
  }
]
```

#### 3.4 즐겨찾기 가게 추가

**엔드포인트**: `POST /api/users/me/favorites`

**설명**: 새로운 가게를 즐겨찾기에 추가합니다.

**인증 필요**: ✅

**요청 본문**:
```json
{
  "storeId": "store003"
}
```

**응답 예시**:
```json
{
  "favStoreId": 3,
  "storeId": "store003",
  "storeName": "분위기 좋은 카페",
  "userId": "user123",
  "createdAt": "2025-08-12T10:20:15.123456"
}
```

**에러 응답** (이미 즐겨찾기된 경우):
```json
{
  "error": "이미 즐겨찾기에 추가된 가게입니다.",
  "timestamp": "2025-08-12T10:20:15.123456"
}
```

#### 3.5 즐겨찾기 가게 삭제

**엔드포인트**: `DELETE /api/users/me/favorites/{storeId}`

**설명**: 즐겨찾기에서 가게를 삭제합니다.

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

#### 3.6 즐겨찾기 상태 확인

**엔드포인트**: `GET /api/users/me/favorites/{storeId}/check`

**설명**: 특정 가게의 즐겨찾기 상태를 확인합니다.

**인증 필요**: ✅

**경로 파라미터**:
- `storeId` (필수): 가게 ID

**응답 예시**:
```json
{
  "isFavorite": true,
  "storeId": "store001"
}
```

#### 3.7 즐겨찾기 가게 개수 조회

**엔드포인트**: `GET /api/users/me/favorites/count`

**설명**: 현재 사용자의 즐겨찾기 가게 개수를 조회합니다.

**인증 필요**: ✅

**응답 예시**:
```json
{
  "count": 3
}
```

### 4. 개발용 API

#### 4.1 더미 데이터 생성

**엔드포인트**: `POST /api/users/dummy/data`

**설명**: 개발 및 테스트를 위한 더미 데이터를 생성합니다.

**인증 필요**: ❌

**응답 예시**:
```json
{
  "success": true,
  "message": "더미 데이터 생성 완료",
  "userId": "dummy8933",
  "favoriteCount": 3
}
```

#### 4.2 서비스 헬스체크

**엔드포인트**: `GET /api/users/health`

**설명**: 서비스의 상태를 확인합니다.

**인증 필요**: ❌

**응답 예시**:
```json
{
  "service": "user-service",
  "status": "UP",
  "timestamp": 1754961236695
}
```

## 📊 응답 코드

### HTTP 상태 코드

| 코드 | 설명 |
|------|------|
| 200 | 성공 |
| 201 | 생성됨 |
| 400 | 잘못된 요청 |
| 401 | 인증 필요 |
| 403 | 권한 없음 |
| 404 | 리소스를 찾을 수 없음 |
| 409 | 충돌 (중복 데이터 등) |
| 500 | 서버 내부 오류 |

### 에러 응답 형식

```json
{
  "error": "에러 메시지",
  "timestamp": "2025-08-12T10:14:10.085936",
  "path": "/api/users/me",
  "status": 400
}
```

## 🔄 MSA 연동

### Store Service 연동

User Service는 Store Service와 연동하여 리뷰 데이터를 가져옵니다.

**Store Service API 호출**:
- `GET /api/stores/reviews/user/{userId}` - 사용자별 리뷰 목록
- `GET /api/stores/reviews/{reviewId}/store-info` - 리뷰별 가게 정보
- `GET /api/stores/{storeId}` - 가게 정보

### 서비스 간 통신

서비스 간 통신은 내부 네트워크를 통해 이루어집니다:

```
User Service (8082) ↔ Store Service (8081)
User Service (8082) ↔ Reservation Service (8080)
```

## 🧪 테스트

### Postman Collection

Postman에서 사용할 수 있는 컬렉션을 제공합니다:

1. **환경 변수 설정**:
   - `baseUrl`: `http://localhost:8082`
   - `accessToken`: 로그인 후 받은 토큰

2. **테스트 시나리오**:
   - 더미 로그인 → 토큰 저장
   - 인증된 API 호출
   - 에러 케이스 테스트

### cURL 예시

```bash
# 더미 로그인
curl -X GET "http://localhost:8082/api/users/login/dummy?state=test-state"

# 내 정보 조회
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8082/api/users/me

# 즐겨찾기 가게 추가
curl -X POST \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"storeId":"store003"}' \
  http://localhost:8082/api/users/me/favorites
```

## 📞 지원

API 관련 문의사항이 있으시면 다음 연락처로 문의해주세요:

- **담당자**: User Service 담당자
- **이메일**: user-service@team-fog.com
- **슬랙**: #user-service
