# 🍽️ 전국 레스토랑 예약 시스템 - User Service

전국 레스토랑 예약 시스템의 사용자 관리 서비스입니다. 마이크로서비스 아키텍처(MSA) 기반으로 구축되었으며, 사용자 회원가입, 로그인, 즐겨찾기 관리 등의 기능을 제공합니다.

## 📋 목차

- [프로젝트 개요](#프로젝트-개요)
- [기술 스택](#기술-스택)
- [아키텍처](#아키텍처)
- [빠른 시작](#빠른-시작)
- [API 문서](#api-문서)
- [배포](#배포)
- [개발 가이드](#개발-가이드)
- [기여하기](#기여하기)

## 🎯 프로젝트 개요

### 주요 기능
- **사용자 관리**: 회원가입, 로그인, 프로필 관리
- **즐겨찾기**: 레스토랑 즐겨찾기 추가/삭제
- **리뷰 연동**: Review Service와의 연동
- **예약 연동**: Booking Service와의 연동
- **지도 연동**: Google Maps API 연동

### MSA 구성
- **User Service** (현재): 사용자 관리
- **Store Service**: 레스토랑 정보 관리
- **Booking Service**: 예약 관리
- **Review Service**: 리뷰 관리
- **Notification Service**: 알림 서비스
- **Gateway Service**: API 게이트웨이

## 🛠️ 기술 스택

### Backend
- **Java 21**
- **Spring Boot 3.4.0**
- **Spring Security**
- **Spring Data JPA**
- **H2 Database** (개발)
- **Oracle Database** (운영)
- **Gradle 8.13**

### Frontend
- **Thymeleaf**
- **Bootstrap 5.3.0**
- **Font Awesome 6.4.0**
- **Google Maps JavaScript API**

### DevOps
- **Docker**
- **AWS ECS**
- **Spring Boot Actuator**

## 🏗️ 아키텍처

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   User Service  │    │   Database      │
│   (Thymeleaf)   │◄──►│   (Spring Boot) │◄──►│   (H2/Oracle)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │   Other MSA     │
                       │   Services      │
                       └─────────────────┘
```

## 🚀 빠른 시작

### 1. 사전 요구사항
- Java 21 이상
- Gradle 8.13 이상
- Git

### 2. 프로젝트 클론
```bash
git clone https://github.com/your-username/restaurant-reservation-system.git
cd restaurant-reservation-system
```

### 3. 로컬 실행
```bash
# Gradle 래퍼 권한 설정 (Linux/Mac)
chmod +x gradlew

# 애플리케이션 실행
./gradlew bootRun
```

### 4. Docker 실행
```bash
# Docker Compose로 전체 서비스 실행
docker-compose up -d

# User Service만 실행
docker-compose up user-service
```

### 5. 접속
- **메인 페이지**: http://localhost:8080
- **H2 콘솔**: http://localhost:8080/h2-console
- **헬스체크**: http://localhost:8080/actuator/health

## 📚 API 문서

### 사용자 관리 API

#### 회원가입
```http
POST /users
Content-Type: application/json

{
  "userId": "user001",
  "userName": "홍길동",
  "phoneNumber": "010-1234-5678",
  "userLocation": "서울시 강남구",
  "password": "password123"
}
```

#### 내 정보 조회
```http
GET /users/me
```

#### 전체 사용자 수 조회
```http
GET /users/count
```

### 인증 API

#### 로그인
```http
POST /login
Content-Type: application/json

{
  "userId": "user001",
  "password": "password123"
}
```

#### 로그아웃
```http
POST /login/logout
```

#### 로그인 상태 확인
```http
GET /login/status
```

### 즐겨찾기 API

#### 즐겨찾기 추가
```http
POST /favorites
Content-Type: application/json

{
  "userId": "user001",
  "storeId": "STORE001"
}
```

#### 즐겨찾기 삭제
```http
DELETE /favorites/{storeId}
```

#### 즐겨찾기 목록 조회
```http
GET /favorites
```

#### 즐겨찾기 여부 확인
```http
GET /favorites/check/{storeId}
```

### 리뷰 API

#### 가게별 리뷰 조회
```http
GET /api/reviews/{storeId}
```

#### 내 리뷰 조회
```http
GET /api/reviews/my
```

## 🚀 배포

### AWS ECS 배포

#### 1. 환경 변수 설정
```bash
# .env 파일 생성
cp .env.example .env

# 환경 변수 수정
DB_HOST=your-oracle-host
DB_PORT=1521
DB_SID=XE
DB_USERNAME=user_service
DB_PASSWORD=your-password
REVIEW_SERVICE_URL=http://review-service:8080
```

#### 2. Docker 이미지 빌드
```bash
# 이미지 빌드
docker build -t user-service .

# ECR에 푸시
aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin your-account.dkr.ecr.ap-northeast-2.amazonaws.com
docker tag user-service:latest your-account.dkr.ecr.ap-northeast-2.amazonaws.com/user-service:latest
docker push your-account.dkr.ecr.ap-northeast-2.amazonaws.com/user-service:latest
```

#### 3. ECS 배포
```bash
# 배포 스크립트 실행
./deploy-ecs.sh
```

### 로컬 Docker 실행
```bash
# 전체 서비스 실행
docker-compose up -d

# 특정 서비스만 실행
docker-compose up user-service oracle-db
```

## 👨‍💻 개발 가이드

### 프로젝트 구조
```
src/
├── main/
│   ├── java/com/restaurant/reservation/
│   │   ├── controller/          # REST API 컨트롤러
│   │   ├── service/            # 비즈니스 로직
│   │   ├── entity/             # JPA 엔티티
│   │   ├── repository/         # 데이터 접근 계층
│   │   ├── dto/               # 데이터 전송 객체
│   │   └── config/            # 설정 클래스
│   └── resources/
│       ├── templates/          # Thymeleaf 템플릿
│       ├── static/            # 정적 리소스
│       └── application.yml    # 설정 파일
└── test/                      # 테스트 코드
```

### 개발 환경 설정

#### 1. IDE 설정
- **IntelliJ IDEA** 권장
- **Spring Boot DevTools** 활성화
- **Lombok** 플러그인 설치

#### 2. 데이터베이스 설정
```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
```

#### 3. 로깅 설정
```yaml
logging:
  level:
    com.restaurant.reservation: DEBUG
    org.springframework.web: DEBUG
```

### 테스트 실행
```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 실행
./gradlew test --tests UserServiceTest

# 테스트 커버리지 확인
./gradlew test jacocoTestReport
```

### 코드 스타일
- **Google Java Style Guide** 준수
- **Spring Boot Code Style** 적용
- **Javadoc** 주석 필수

## 🤝 기여하기

### 1. Fork & Clone
```bash
git clone https://github.com/your-username/restaurant-reservation-system.git
cd restaurant-reservation-system
```

### 2. 브랜치 생성
```bash
git checkout -b feature/your-feature-name
```

### 3. 개발 및 테스트
```bash
# 개발
./gradlew bootRun

# 테스트
./gradlew test
```

### 4. 커밋 및 푸시
```bash
git add .
git commit -m "feat: 새로운 기능 추가"
git push origin feature/your-feature-name
```

### 5. Pull Request 생성
GitHub에서 Pull Request를 생성하고 리뷰를 요청합니다.

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

## 📞 문의

- **이슈 리포트**: [GitHub Issues](https://github.com/your-username/restaurant-reservation-system/issues)
- **이메일**: your-email@example.com
- **팀**: FOG Team

## 🙏 감사의 말

- **Spring Boot** 팀
- **Google Maps API** 팀
- **Bootstrap** 팀
- **Font Awesome** 팀

---

**FOG Team** © 2024 - 전국 레스토랑 예약 시스템
