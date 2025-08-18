# 🍽️ FOG Restaurant Reservation System - User Service

FOG 팀의 식당 예약 시스템 중 **User Service**입니다. AWS Cognito를 통한 사용자 인증과 사용자 정보 관리를 담당합니다.

## 🚀 주요 기능

- **AWS Cognito 인증**: OAuth2 Authorization Code Flow를 통한 안전한 사용자 인증
- **사용자 관리**: 회원가입, 정보 수정, 프로필 관리
- **즐겨찾기 관리**: 가게 즐겨찾기 추가/삭제/조회
- **마이페이지**: 통합 사용자 대시보드
- **MSA 연동**: Store Service, Reservation Service와의 연동

## 🛠️ 기술 스택

- **Backend**: Spring Boot 3.x, Spring Security, JPA/Hibernate
- **Database**: Oracle Database (EC2)
- **Authentication**: AWS Cognito
- **Container**: Docker
- **Deployment**: AWS ECS Fargate
- **Load Balancer**: AWS ALB

## 📋 프로젝트 구조

```
src/main/java/com/restaurant/reservation/
├── config/          # 설정 클래스들
├── controller/      # REST API 컨트롤러
├── dto/            # 데이터 전송 객체
├── entity/         # JPA 엔티티
├── repository/     # 데이터 접근 계층
└── service/        # 비즈니스 로직
```

## 🚀 빠른 시작

### 1. 프로젝트 빌드
```bash
# Windows
gradlew.bat clean build

# Linux/Mac
./gradlew clean build
```

### 2. 애플리케이션 실행
```bash
# Windows
gradlew.bat bootRun

# Linux/Mac
./gradlew bootRun
```

### 3. 테스트
```bash
# Windows
gradlew.bat test

# Linux/Mac
./gradlew test
```

## 📖 상세 가이드

- **[SETUP_GUIDE.md](SETUP_GUIDE.md)**: 개발 환경 설정 가이드
- **[TEST_GUIDE.md](TEST_GUIDE.md)**: 테스트 및 API 테스트 가이드
- **[PRODUCTION_README.md](PRODUCTION_README.md)**: 프로덕션 배포 가이드

## 🔧 설정

### 필수 환경 변수
```yaml
# AWS Cognito 설정
aws:
  cognito:
    user-pool-id: ap-northeast-2_xxxxx
    client-id: xxxxxxxxxx
    client-secret: xxxxxxxxxx
    domain: https://xxxxx.auth.ap-northeast-2.amazoncognito.com
    region: ap-northeast-2
    jwks-url: https://cognito-idp.ap-northeast-2.amazonaws.com/xxxxx/.well-known/jwks.json

# Oracle Database 설정
spring:
  datasource:
    url: jdbc:oracle:thin:@localhost:1521:XE
    username: your_username
    password: your_password
```

## 📡 API 엔드포인트

### 인증 관련
- `GET /api/users/login/url` - Cognito 로그인 URL 생성
- `POST /api/users/login/callback` - Cognito 콜백 처리
- `POST /api/users/logout` - 로그아웃

### 사용자 관리
- `POST /api/users` - 회원가입
- `GET /api/users/me` - 마이페이지 조회
- `PUT /api/users/me` - 사용자 정보 수정

### 즐겨찾기 관리
- `GET /api/users/me/favorites` - 즐겨찾기 목록 조회
- `POST /api/users/me/favorites` - 즐겨찾기 추가
- `DELETE /api/users/me/favorites/{storeId}` - 즐겨찾기 삭제

## 🐳 Docker 배포

```bash
# Docker 이미지 빌드
docker build -t fog-user-service .

# 컨테이너 실행
docker run -p 8080:8080 fog-user-service
```

## 📊 데이터베이스 스키마

### USERS 테이블
```sql
CREATE TABLE USERS (
    USER_ID VARCHAR2(50) PRIMARY KEY,
    USER_NAME VARCHAR2(20) NOT NULL,
    PHONE_NUMBER VARCHAR2(20) NOT NULL UNIQUE,
    USER_LOCATION VARCHAR2(50)
);
```

### FAV_STORE 테이블
```sql
CREATE TABLE FAV_STORE (
    FAV_STORE_ID NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    USER_ID VARCHAR2(15) NOT NULL,
    STORE_ID2 VARCHAR2(20) NOT NULL,
    STORE_NAME VARCHAR2(100),
    CONSTRAINT UK_FAV_STORE_USER_STORE UNIQUE (USER_ID, STORE_ID2)
);
```

## 🤝 팀 정보

- **팀명**: FOG (Food Order Group)
- **프로젝트**: 식당 예약 시스템
- **서비스**: User Service (사용자 관리 서비스)

## 📝 라이선스

이 프로젝트는 FOG 팀의 내부 프로젝트입니다.

---

**FOG Team** | 2025
