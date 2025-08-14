# 🚀 User Service 실제 배포환경 설정 가이드

## 📋 개요

이 문서는 User Service를 실제 AWS MSA 환경에 배포하기 위한 설정 가이드입니다.

## 🔧 실제 배포환경 설정 단계

### 1. Oracle DB 의존성 활성화

**build.gradle** 파일에서 Oracle DB 의존성 주석을 해제합니다:

```gradle
// =============================================================================
// Oracle DB 의존성 (AWS MSA 프로덕션 환경용)
// 실제 배포환경 설정
// =============================================================================
runtimeOnly 'com.oracle.database.jdbc:ojdbc11:23.3.0.23.09'
runtimeOnly 'com.oracle.database.security:oraclepki:23.3.0.23.09'
runtimeOnly 'com.oracle.database.security:osdt_core:23.3.0.23.09'
runtimeOnly 'com.oracle.database.security:osdt_cert:23.3.0.23.09'
```

### 2. Oracle 설정 클래스 활성화

**OracleConfig.java** 파일에서 어노테이션 주석을 해제합니다:

```java
@Configuration
@ConfigurationProperties(prefix = "oracle")
public class OracleConfig {
```

**OracleDataSourceConfig.java** 파일에서 어노테이션 주석을 해제합니다:

```java
@Configuration
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "prod")
public class OracleDataSourceConfig {
```

### 3. application.yml 설정 변경

**src/main/resources/application.yml** 파일에서 다음 설정을 변경합니다:

#### 3.1 AWS Cognito 설정
```yaml
aws:
  cognito:
    # 실제 배포환경 설정 (false: 실제 Cognito 사용)
    dummy-mode: false
    region: ap-northeast-2
    user-pool-id: ${COGNITO_USER_POOL_ID}
    client-id: ${COGNITO_CLIENT_ID}
    client-secret: ${COGNITO_CLIENT_SECRET}
    domain: ${COGNITO_DOMAIN}
    redirect-uri: ${FRONTEND_URL}/callback
```

#### 3.2 Oracle Database 설정
```yaml
# =============================================================================
# Oracle Database 설정 (AWS MSA 환경용)
# 실제 배포환경 설정
# =============================================================================
oracle:
  host: ${ORACLE_HOST:primarydb.internal}
  port: ${ORACLE_PORT:1521}
  service-name: ${ORACLE_SERVICE_NAME:TEAMFOG}
  username: ${ORACLE_USERNAME:user_service}
  password: ${ORACLE_PASSWORD}
  max-pool-size: ${ORACLE_MAX_POOL_SIZE:20}
  min-pool-size: ${ORACLE_MIN_POOL_SIZE:5}
  connection-timeout: ${ORACLE_CONNECTION_TIMEOUT:30000}
  idle-timeout: ${ORACLE_IDLE_TIMEOUT:600000}
  ssl-enabled: ${ORACLE_SSL_ENABLED:false}
  
  # StandbyDB 설정 (읽기 전용)
  standby:
    host: ${ORACLE_STANDBY_HOST:standbydb.internal}
    port: ${ORACLE_STANDBY_PORT:1521}
    service-name: ${ORACLE_STANDBY_SERVICE_NAME:TEAMFOG}
    username: ${ORACLE_STANDBY_USERNAME:user_service_readonly}
    password: ${ORACLE_STANDBY_PASSWORD}
```

#### 3.3 MSA 서비스 URL 설정
```yaml
msa:
  service-urls:
    reservation-service: ${RESERVATION_SERVICE_URL:http://reservation-service.internal:8080}
    store-service: ${STORE_SERVICE_URL:http://store-service.internal:8081}
    frontend: ${FRONTEND_URL:https://team-fog-frontend.com}
```

### 4. application-prod.yml 설정 변경

**src/main/resources/application-prod.yml** 파일에서 다음 설정을 변경합니다:

#### 4.1 Oracle Database 설정
```yaml
# =============================================================================
# Oracle Database 설정 (AWS MSA 프로덕션 환경)
# 실제 배포환경 설정
# =============================================================================
datasource:
  url: jdbc:oracle:thin:@${ORACLE_HOST}:${ORACLE_PORT}:${ORACLE_SERVICE_NAME}
  driver-class-name: oracle.jdbc.OracleDriver
  username: ${ORACLE_USERNAME}
  password: ${ORACLE_PASSWORD}
  hikari:
    maximum-pool-size: ${ORACLE_MAX_POOL_SIZE:30}
    minimum-idle: ${ORACLE_MIN_POOL_SIZE:10}
    connection-timeout: ${ORACLE_CONNECTION_TIMEOUT:60000}
    idle-timeout: ${ORACLE_IDLE_TIMEOUT:1200000}
    ssl: ${ORACLE_SSL_ENABLED:true}

jpa:
  hibernate:
    ddl-auto: validate  # 프로덕션에서는 validate 사용
  show-sql: false  # 프로덕션에서는 false
  properties:
    hibernate:
      dialect: org.hibernate.dialect.OracleDialect
      format_sql: false

oracle:
  host: ${ORACLE_HOST:primarydb.internal}
  port: ${ORACLE_PORT:1521}
  service-name: ${ORACLE_SERVICE_NAME:TEAMFOG}
  username: ${ORACLE_USERNAME:user_service}
  password: ${ORACLE_PASSWORD}
  max-pool-size: ${ORACLE_MAX_POOL_SIZE:30}
  min-pool-size: ${ORACLE_MIN_POOL_SIZE:10}
  connection-timeout: ${ORACLE_CONNECTION_TIMEOUT:60000}
  idle-timeout: ${ORACLE_IDLE_TIMEOUT:1200000}
  ssl-enabled: ${ORACLE_SSL_ENABLED:true}
  
  # StandbyDB 설정 (읽기 전용)
  standby:
    host: ${ORACLE_STANDBY_HOST:standbydb.internal}
    port: ${ORACLE_STANDBY_PORT:1521}
    service-name: ${ORACLE_STANDBY_SERVICE_NAME:TEAMFOG}
    username: ${ORACLE_STANDBY_USERNAME:user_service_readonly}
    password: ${ORACLE_STANDBY_PASSWORD}
```

### 5. 환경변수 설정

실제 배포환경에서 다음 환경변수를 설정해야 합니다:

```bash
# =============================================================================
# AWS Cognito 설정
# =============================================================================
COGNITO_USER_POOL_ID=ap-northeast-2_xxxxxxxxx
COGNITO_CLIENT_ID=xxxxxxxxxxxxxxxxxxxxxxxxxxx
COGNITO_CLIENT_SECRET=xxxxxxxxxxxxxxxxxxxxxxxxxxx
COGNITO_DOMAIN=team-fog.auth.ap-northeast-2.amazoncognito.com

# =============================================================================
# Oracle Database 설정 (PDB + Standby)
# =============================================================================
# PrimaryDB
ORACLE_HOST=primarydb.internal
ORACLE_PORT=1521
ORACLE_SERVICE_NAME=TEAMFOG
ORACLE_USERNAME=user_service
ORACLE_PASSWORD=xxxxxxxxxxxx

# StandbyDB (Read-Only)
ORACLE_STANDBY_HOST=standbydb.internal
ORACLE_STANDBY_PORT=1521
ORACLE_STANDBY_SERVICE_NAME=TEAMFOG
ORACLE_STANDBY_USERNAME=user_service_readonly
ORACLE_STANDBY_PASSWORD=xxxxxxxxxxxx

# =============================================================================
# MSA 서비스 URL 설정
# =============================================================================
RESERVATION_SERVICE_URL=http://reservation-service.internal:8080
STORE_SERVICE_URL=http://store-service.internal:8081
FRONTEND_URL=https://team-fog-frontend.com

# =============================================================================
# Spring Profile 설정
# =============================================================================
SPRING_PROFILES_ACTIVE=prod
```

### 6. 더미 모드 비활성화

실제 배포환경에서는 더미 모드가 자동으로 비활성화됩니다:

- `aws.cognito.dummy-mode: false`로 설정
- 더미 로그인 API (`/api/users/login/dummy`) 비활성화
- 더미 데이터 생성 API (`/api/users/dummy/data`) 비활성화

### 7. 빌드 및 배포

#### 7.1 Docker 이미지 빌드
```bash
# Docker 이미지 빌드
docker build -t team-fog-user-service:latest .

# ECR에 푸시
aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin ${ECR_REPOSITORY_URI}
docker tag team-fog-user-service:latest ${ECR_REPOSITORY_URI}:latest
docker push ${ECR_REPOSITORY_URI}:latest
```

#### 7.2 ECS 서비스 배포
```bash
# ECS 서비스 업데이트
aws ecs update-service --cluster team-fog-cluster --service user-service --force-new-deployment
```

## 🔍 배포 후 확인사항

### 1. 서비스 상태 확인
```bash
# ECS 서비스 상태 확인
aws ecs describe-services --cluster team-fog-cluster --services user-service

# 로그 확인
aws logs tail /ecs/user-service --follow
```

### 2. API 테스트
```bash
# 헬스체크
curl -X GET http://user-service.internal:8082/actuator/health

# Cognito 로그인 URL 생성
curl -X GET http://user-service.internal:8082/api/users/login/url

# 사용자 수 조회
curl -X GET http://user-service.internal:8082/api/users/count
```

### 3. 데이터베이스 연결 확인
```bash
# Oracle DB 연결 테스트
sqlplus user_service/password@primarydb.internal:1521/TEAMFOG

# 테이블 확인
SELECT COUNT(*) FROM USERS;
SELECT COUNT(*) FROM FAV_STORE;
```

## 🚨 문제 해결

### 1. Oracle DB 연결 실패
- Oracle DB ECS 서비스 상태 확인
- 보안 그룹 설정 확인 (포트 1521)
- PDB 연결 확인
- 사용자 권한 확인

### 2. Cognito 연결 실패
- User Pool ID, Client ID 확인
- 네트워크 보안 그룹 설정 확인
- IAM 권한 확인

### 3. MSA 서비스 연결 실패
- 서비스 URL 확인
- 네트워크 설정 확인
- 서킷 브레이커 설정 확인

## 📝 체크리스트

- [ ] Oracle DB 의존성 활성화
- [ ] Oracle 설정 클래스 활성화
- [ ] application.yml 설정 변경
- [ ] application-prod.yml 설정 변경
- [ ] 환경변수 설정
- [ ] Docker 이미지 빌드
- [ ] ECR 푸시
- [ ] ECS 서비스 배포
- [ ] 서비스 상태 확인
- [ ] API 테스트
- [ ] 데이터베이스 연결 확인

## 📞 지원

문제가 발생하면 다음 연락처로 문의하세요:
- **담당자**: User Service 담당자
- **이메일**: user-service@team-fog.com
- **슬랙**: #user-service
