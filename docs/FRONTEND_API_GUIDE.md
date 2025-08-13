# 🚀 Team-FOG User Service API 연동 가이드

## 📋 개요
이 문서는 프론트엔드 팀이 User Service와 연동하기 위한 API 가이드입니다.

## 🔧 환경 설정

### 1. 프록시 설정 수정
```javascript
// vue.config.js 수정 필요
module.exports = defineConfig({
  devServer: {
    proxy: {
      '/api/users': {
        target: 'http://localhost:8082',  // User Service 포트
        changeOrigin: true
      },
      '/api/stores': {
        target: 'http://localhost:8081',  // Store Service 포트
        changeOrigin: true
      },
      '/api/bookings': {
        target: 'http://localhost:8080',  // Reservation Service 포트
        changeOrigin: true
      }
    }
  }
});
```

### 2. 환경변수 설정
```bash
# .env 파일 생성
VUE_APP_API_BASE_URL=http://localhost:8082/api
VUE_APP_COGNITO_USER_POOL_ID=your-user-pool-id
VUE_APP_COGNITO_CLIENT_ID=your-client-id
```

## 🔐 인증 관련 API

### 1. 더미 로그인 (개발용)
```javascript
// GET /api/users/login/dummy?state=random-state
const response = await axios.get('/api/users/login/dummy', {
  params: { state: 'random-state' }
});

// 응답 형식
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

### 2. Cognito 로그인 URL 생성
```javascript
// GET /api/users/login/url
const response = await axios.get('/api/users/login/url');

// 응답 형식
{
  "url": "https://cognito-login-url",
  "state": "random-state"
}
```

### 3. 로그아웃
```javascript
// POST /api/users/logout
const response = await axios.post('/api/users/logout');

// 응답 형식
{
  "success": true,
  "message": "로그아웃 성공"
}
```

## 👤 사용자 정보 API

### 1. 내 정보 조회
```javascript
// GET /api/users/me
const response = await axios.get('/api/users/me', {
  headers: {
    'Authorization': 'Bearer ' + accessToken
  }
});

// 응답 형식
{
  "userId": "user123",
  "userName": "홍길동",
  "phoneNumber": "010-1234-5678",
  "userLocation": "서울시 강남구",
  "active": true,
  "createdAt": "2024-01-15T10:00:00",
  "updatedAt": "2024-01-15T10:00:00"
}
```

### 2. 사용자 정보 수정
```javascript
// PUT /api/users/me
const response = await axios.put('/api/users/me', {
  userName: "새로운 이름",
  phoneNumber: "010-9876-5432",
  userLocation: "서울시 서초구"
}, {
  headers: {
    'Authorization': 'Bearer ' + accessToken
  }
});
```

## 📊 대시보드 API

### 1. 통계 데이터 조회
```javascript
// GET /api/users/dashboard/counts
const response = await axios.get('/api/users/dashboard/counts');

// 응답 형식
{
  "stores": 15,
  "members": 250,
  "bookings": 89
}
```

## ⭐ 즐겨찾기 API

### 1. 즐겨찾기 가게 목록 조회
```javascript
// GET /api/users/me/favorites
const response = await axios.get('/api/users/me/favorites', {
  headers: {
    'Authorization': 'Bearer ' + accessToken
  }
});

// 응답 형식
[
  {
    "id": 1,
    "storeId": "store001",
    "storeName": "맛있는 한식당",
    "userId": "user123",
    "createdAt": "2024-01-15T10:00:00"
  }
]
```

### 2. 즐겨찾기 가게 추가
```javascript
// POST /api/users/me/favorites
const response = await axios.post('/api/users/me/favorites', {
  storeId: "store001"
}, {
  headers: {
    'Authorization': 'Bearer ' + accessToken
  }
});
```

### 3. 즐겨찾기 가게 삭제
```javascript
// DELETE /api/users/me/favorites/store001
const response = await axios.delete('/api/users/me/favorites/store001', {
  headers: {
    'Authorization': 'Bearer ' + accessToken
  }
});
```

### 4. 즐겨찾기 상태 확인
```javascript
// GET /api/users/me/favorites/store001/check
const response = await axios.get('/api/users/me/favorites/store001/check', {
  headers: {
    'Authorization': 'Bearer ' + accessToken
  }
});

// 응답 형식
{
  "isFavorite": true,
  "storeId": "store001"
}
```

### 5. 즐겨찾기 개수 조회
```javascript
// GET /api/users/me/favorites/count
const response = await axios.get('/api/users/me/favorites/count', {
  headers: {
    'Authorization': 'Bearer ' + accessToken
  }
});

// 응답 형식
{
  "count": 5
}
```

## 📝 리뷰 API

### 1. 내 리뷰 목록 조회
```javascript
// GET /api/users/me/reviews
const response = await axios.get('/api/users/me/reviews', {
  headers: {
    'Authorization': 'Bearer ' + accessToken
  }
});

// 응답 형식
[
  {
    "reviewId": 1,
    "storeId": "store001",
    "storeName": "맛있는 한식당",
    "userId": "user123",
    "userName": "홍길동",
    "content": "정말 맛있었어요!",
    "rating": 5,
    "createdAt": "2024-01-15T10:00:00",
    "updatedAt": "2024-01-15T10:00:00"
  }
]
```

## 🔧 Axios 인터셉터 설정

### 1. 요청 인터셉터
```javascript
// main.js 또는 별도 파일
axios.interceptors.request.use(
  config => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);
```

### 2. 응답 인터셉터
```javascript
axios.interceptors.response.use(
  response => {
    return response;
  },
  error => {
    if (error.response?.status === 401) {
      // 토큰 만료 시 로그인 페이지로 리다이렉트
      router.push('/login');
    }
    return Promise.reject(error);
  }
);
```

## 🚨 에러 처리

### 1. 공통 에러 응답 형식
```javascript
{
  "success": false,
  "message": "에러 메시지",
  "errorCode": "ERROR_CODE",
  "timestamp": "2024-01-15T10:00:00"
}
```

### 2. HTTP 상태 코드
- `200`: 성공
- `201`: 생성 성공
- `400`: 잘못된 요청
- `401`: 인증 실패
- `403`: 권한 없음
- `404`: 리소스 없음
- `500`: 서버 오류

## 🧪 테스트 방법

### 1. 더미 로그인 테스트
```javascript
// 개발 환경에서 더미 로그인으로 테스트
const loginResponse = await axios.get('/api/users/login/dummy', {
  params: { state: 'test-state' }
});

if (loginResponse.data.success) {
  localStorage.setItem('accessToken', loginResponse.data.accessToken);
  // 이후 인증이 필요한 API 호출 가능
}
```

### 2. API 테스트
```javascript
// 내 정보 조회 테스트
const myInfoResponse = await axios.get('/api/users/me');
console.log('내 정보:', myInfoResponse.data);

// 대시보드 통계 테스트
const dashboardResponse = await axios.get('/api/users/dashboard/counts');
console.log('대시보드:', dashboardResponse.data);
```

## 📞 문의사항
- User Service 담당자에게 문의
- API 문서 업데이트 필요 시 알려주세요
