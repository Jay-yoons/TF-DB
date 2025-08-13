# 🚀 Team-FOG User Service 설정 가이드

## 📋 개요

이 가이드는 Team-FOG User Service를 로컬에서 실행하기 위한 설정 방법을 설명합니다.

## 📁 설정 파일 목록

### 1. **application.yml** - 메인 설정 파일
- 모든 환경에서 공통으로 사용하는 기본 설정
- 프로파일별 설정 포함 (dev, test, prod)

### 2. **application-dev.yml** - 개발 환경 설정
- 로컬 개발 시 사용
- H2 Database, 더미 Cognito 모드

### 3. **application-prod.yml** - 프로덕션 환경 설정
- AWS ECS 배포 시 사용
- Oracle Database, 실제 Cognito

### 4. **env-example.txt** - 환경변수 예시
- 환경변수 설정 참고용

## 🔧 설정 방법

### 1단계: 설정 파일 복사

```bash
# 프로젝트 루트 디렉토리에서
cp application.yml src/main/resources/
cp application-dev.yml src/main/resources/
cp application-prod.yml src/main/resources/
```

### 2단계: 환경변수 설정

```bash
# env-example.txt를 참고하여 .env 파일 생성
cp env-example.txt .env

# .env 파일을 편집하여 실제 값으로 수정
# 예시:
SPRING_PROFILES_ACTIVE=dev
AWS_COGNITO_DUMMY_MODE=true
```

### 3단계: 애플리케이션 실행

```bash
# 개발 환경으로 실행
./gradlew bootRun --args='--spring.profiles.active=dev'

# 또는 환경변수로 설정
export SPRING_PROFILES_ACTIVE=dev
./gradlew bootRun
```

## 🎯 환경별 설정

### 🛠️ **개발 환경 (dev)**

**특징:**
- H2 인메모리 데이터베이스 사용
- AWS Cognito 더미 모드
- 상세한 로깅 활성화
- H2 Console 접근 가능

**설정:**
```yaml
spring:
  profiles:
    active: dev
```

**접속 정보:**
- 서버: http://localhost:8082
- H2 Console: http://localhost:8082/h2-console
- JDBC URL: jdbc:h2:mem:userdb
- Username: sa
- Password: (비어있음)

### 🧪 **테스트 환경 (test)**

**특징:**
- H2 인메모리 데이터베이스
- 테스트용 로깅 설정
- 더미 Cognito 모드

**설정:**
```yaml
spring:
  profiles:
    active: test
```

### 🚀 **프로덕션 환경 (prod)**

**특징:**
- Oracle Database 사용
- 실제 AWS Cognito 연동
- 보안 강화 설정
- 성능 최적화

**설정:**
```yaml
spring:
  profiles:
    active: prod
```

## 🔐 AWS Cognito 설정

### 개발용 더미 모드
```yaml
aws:
  cognito:
    dummy-mode: true
```

**더미 로그인 테스트:**
```bash
curl -X GET "http://localhost:8082/api/users/login/dummy?state=test-state"
```

### 프로덕션용 실제 Cognito
```yaml
aws:
  cognito:
    dummy-mode: false
    user-pool-id: ${COGNITO_USER_POOL_ID}
    client-id: ${COGNITO_CLIENT_ID}
    client-secret: ${COGNITO_CLIENT_SECRET}
    domain: ${COGNITO_DOMAIN}
```

## 🗄️ 데이터베이스 설정

### 개발용 H2 Database
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:userdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
```

### 프로덕션용 Oracle Database
```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@${ORACLE_HOST}:${ORACLE_PORT}/${ORACLE_SERVICE_NAME}
    driver-class-name: oracle.jdbc.OracleDriver
    username: ${ORACLE_USERNAME}
    password: ${ORACLE_PASSWORD}
```

## 🔄 MSA 서비스 연동

### 로컬 개발 환경
```yaml
msa:
  service-urls:
    store-service: http://localhost:8081
    reservation-service: http://localhost:8080
```

### 프로덕션 환경
```yaml
msa:
  service-urls:
    store-service: http://store-service.internal:8081
    reservation-service: http://reservation-service.internal:8080
```

## 🧪 테스트 방법

### 1. 서비스 헬스체크
```bash
curl -X GET "http://localhost:8082/api/users/health"
```

### 2. 더미 로그인
```bash
curl -X GET "http://localhost:8082/api/users/login/dummy?state=test-state"
```

### 3. 더미 데이터 생성
```bash
curl -X POST "http://localhost:8082/api/users/dummy/data"
```

### 4. 인증된 API 테스트
```bash
# 토큰으로 API 호출
curl -H "Authorization: Bearer dummy-access-token-1234567890" \
  http://localhost:8082/api/users/me
```

## 🚨 문제 해결

### 1. 포트 충돌
```bash
# 8082 포트 사용 중인 프로세스 확인
netstat -ano | findstr :8082

# 프로세스 종료
taskkill /f /im java.exe
```

### 2. 데이터베이스 연결 실패
- H2 Console 접속 확인: http://localhost:8082/h2-console
- Oracle DB 연결 정보 확인
- 네트워크 설정 확인

### 3. Cognito 연결 실패
- 더미 모드 활성화 확인
- 환경변수 설정 확인
- AWS 자격 증명 확인

## 📞 지원

- **담당자**: User Service 담당자
- **이메일**: user-service@team-fog.com
- **슬랙**: #user-service

---

이 가이드를 따라 User Service를 성공적으로 설정하고 실행할 수 있습니다! 🚀
