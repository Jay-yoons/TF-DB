# 🚀 Team-FOG AWS ECS MSA 통합 가이드

## 📋 개요

이 문서는 Team-FOG 프로젝트의 모든 서비스가 AWS ECS 기반 MSA 환경에서 원활하게 작동할 수 있도록 하는 종합적인 가이드입니다.

**분석된 브랜치:**
- `user_sample` (User Service)
- `store_sample_v2` (Store Service)  
- `sample` (Booking Service + Frontend)

**최종 업데이트**: 2025년 8월 14일

## 🏗️ 전체 아키텍처 개요

### MSA 서비스 구성
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   User Service  │    │  Store Service  │
│   (Vue.js)      │◄──►│   (Port: 8082)  │◄──►│   (Port: 8081)  │
│   (Port: 3000)  │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ Booking Service │    │ Oracle Primary  │    │ Oracle Standby  │
│ (Port: 8080)    │    │     DB          │    │      DB         │
│                 │    │ (ECS EC2)       │    │ (ECS EC2)       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### AWS ECS 서비스 배포 구조
```
AWS ECS Cluster: team-fog-cluster
├── User Service (Fargate)
├── Store Service (Fargate)  
├── Booking Service (Fargate)
├── Frontend (Fargate)
├── PrimaryDB (EC2)
└── StandbyDB (EC2)
```

## 🔧 서비스별 상세 분석

### 1. User Service (`user_sample` 브랜치)

#### ✅ **현재 상태: 완전 준비됨**
- **포트**: 8082
- **기술 스택**: Spring Boot 3.5.4, Java 17, Oracle DB
- **배포 방식**: AWS ECS Fargate

#### 🔧 **주요 구성 요소**
```yaml
# application.yml (개발 환경)
spring:
  datasource:
    url: jdbc:h2:mem:userdb  # 개발용 H2
  profiles:
    active: dev

# application-prod.yml (프로덕션 환경)  
spring:
  datasource:
    url: jdbc:oracle:thin:@${ORACLE_HOST}:${ORACLE_PORT}:${ORACLE_SERVICE_NAME}
    driver-class-name: oracle.jdbc.OracleDriver
```

#### 🔐 **인증 시스템**
- **개발용**: 더미 Cognito 모드 (`dummy-mode: true`)
- **프로덕션**: 실제 AWS Cognito 연동
- **JWT 토큰**: Bearer 토큰 방식

#### 📊 **API 엔드포인트**
```bash
# 인증
GET /api/users/login/dummy?state=test-state
GET /api/users/login/url
POST /api/users/login/callback
POST /api/users/logout

# 사용자 관리
GET /api/users/me                    # 통합 마이페이지
PUT /api/users/me                    # 정보 수정
POST /api/users                      # 회원가입

# 즐겨찾기
GET /api/users/me/favorites
POST /api/users/me/favorites
DELETE /api/users/me/favorites/{storeId}

# 헬스체크
GET /api/users/health
```

#### 🔗 **MSA 연동**
```yaml
msa:
  service-urls:
    store-service: http://store-service.internal:8081
    reservation-service: http://reservation-service.internal:8080
```

### 2. Store Service (`store_sample_v2` 브랜치)

#### ✅ **현재 상태: 준비됨**
- **포트**: 8081
- **기술 스택**: Spring Boot, Oracle DB
- **배포 방식**: AWS ECS Fargate

#### 🔧 **주요 구성 요소**
```yaml
# Store Service 설정
spring:
  datasource:
    url: jdbc:oracle:thin:@${ORACLE_HOST}:${ORACLE_PORT}:${ORACLE_SERVICE_NAME}
    username: ${ORACLE_USERNAME:store_service}
```

#### 📊 **API 엔드포인트**
```bash
# 가게 관리
GET /api/stores
GET /api/stores/{storeId}
POST /api/stores

# 리뷰 시스템
GET /api/stores/reviews
POST /api/stores/reviews
GET /api/stores/reviews/user/{userId}

# 메뉴 관리
GET /api/stores/{storeId}/menus
POST /api/stores/{storeId}/menus

# 헬스체크
GET /api/stores/health
```

#### 🔗 **MSA 연동**
```yaml
# User Service 연동
USER_SERVICE_URL: http://user-service.internal:8082
RESERVATION_SERVICE_URL: http://reservation-service.internal:8080
```

### 3. Booking Service (`sample` 브랜치)

#### ✅ **현재 상태: 준비됨**
- **포트**: 8080
- **기술 스택**: Spring Boot, Oracle DB
- **배포 방식**: AWS ECS Fargate

#### 🔧 **주요 구성 요소**
```yaml
# Booking Service 설정
spring:
  datasource:
    url: jdbc:oracle:thin:@${ORACLE_HOST}:${ORACLE_PORT}:${ORACLE_SERVICE_NAME}
    username: ${ORACLE_USERNAME:reservation_service}
```

#### 📊 **API 엔드포인트**
```bash
# 예약 관리
GET /api/bookings
POST /api/bookings
GET /api/bookings/{bookingId}
PUT /api/bookings/{bookingId}
DELETE /api/bookings/{bookingId}

# 예약 상태
GET /api/bookings/status/{bookingId}
PUT /api/bookings/{bookingId}/status

# 헬스체크
GET /api/bookings/health
```

#### 🔗 **MSA 연동**
```yaml
# 다른 서비스 연동
USER_SERVICE_URL: http://user-service.internal:8082
STORE_SERVICE_URL: http://store-service.internal:8081
```

### 4. Frontend (`sample` 브랜치)

#### ✅ **현재 상태: 준비됨**
- **포트**: 3000
- **기술 스택**: Vue.js 3, Axios, AWS Amplify
- **배포 방식**: AWS ECS Fargate

#### 🔧 **주요 구성 요소**
```javascript
// vue.config.js
module.exports = defineConfig({
  devServer: {
    proxy: {
      '/api/users': {
        target: 'http://localhost:8082',
        changeOrigin: true
      },
      '/api/stores': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/api/bookings': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
});
```

#### 🔐 **인증 시스템**
```javascript
// AWS Amplify 설정
import { Amplify } from 'aws-amplify';
import awsconfig from './aws-exports';

Amplify.configure(awsconfig);
```

## 🗄️ 데이터베이스 설정

### Oracle Database (ECS EC2 기반)

#### PrimaryDB 설정
```json
{
  "family": "primarydb",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["EC2"],
  "cpu": "4096",
  "memory": "8192",
  "containerDefinitions": [
    {
      "name": "primarydb",
      "image": "container-registry.oracle.com/database/enterprise:21.3.0.0",
      "portMappings": [
        {"containerPort": 1521, "protocol": "tcp"},
        {"containerPort": 5500, "protocol": "tcp"}
      ],
      "environment": [
        {"name": "ORACLE_PWD", "value": "Oracle123456"},
        {"name": "ORACLE_CHARACTERSET", "value": "AL32UTF8"},
        {"name": "ORACLE_EDITION", "value": "enterprise"}
      ]
    }
  ]
}
```

#### StandbyDB 설정
```json
{
  "family": "standbydb",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["EC2"],
  "cpu": "4096",
  "memory": "8192",
  "containerDefinitions": [
    {
      "name": "standbydb",
      "image": "container-registry.oracle.com/database/enterprise:21.3.0.0",
      "environment": [
        {"name": "STANDBY_MODE", "value": "true"}
      ]
    }
  ]
}
```

### 데이터베이스 스키마
```sql
-- PDB 생성
CREATE PLUGGABLE DATABASE TEAMFOG
ADMIN USER teamfog_admin IDENTIFIED BY "admin_password_here"
STORAGE (MAXSIZE 2G)
DEFAULT TABLESPACE teamfog_data;

-- 서비스별 사용자 생성
CREATE USER user_service IDENTIFIED BY "user_password_here";
CREATE USER store_service IDENTIFIED BY "store_password_here";
CREATE USER reservation_service IDENTIFIED BY "reservation_password_here";
```

## 🚀 AWS ECS 배포 설정

### 1. ECS Task Definitions

#### User Service Task Definition
```json
{
  "family": "user-service",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "containerDefinitions": [
    {
      "name": "user-service",
      "image": "YOUR_ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/user-service:latest",
      "portMappings": [
        {"containerPort": 8082, "protocol": "tcp"}
      ],
      "environment": [
        {"name": "SPRING_PROFILES_ACTIVE", "value": "prod"},
        {"name": "TZ", "value": "Asia/Seoul"},
        {"name": "JAVA_OPTS", "value": "-Duser.timezone=Asia/Seoul -Dfile.encoding=UTF-8"},
        {"name": "ORACLE_HOST", "value": "primarydb.internal"},
        {"name": "ORACLE_PORT", "value": "1521"},
        {"name": "ORACLE_SERVICE_NAME", "value": "TEAMFOG"},
        {"name": "ORACLE_USERNAME", "value": "user_service"},
        {"name": "COGNITO_USER_POOL_ID", "value": "ap-northeast-2_xxxxxxxxx"},
        {"name": "COGNITO_CLIENT_ID", "value": "xxxxxxxxxxxxxxxxxxxxxxxxxxx"},
        {"name": "RESERVATION_SERVICE_URL", "value": "http://reservation-service.internal:8080"},
        {"name": "STORE_SERVICE_URL", "value": "http://store-service.internal:8081"}
      ],
      "secrets": [
        {"name": "ORACLE_PASSWORD", "valueFrom": "arn:aws:secretsmanager:ap-northeast-2:YOUR_ACCOUNT_ID:secret:team-fog/oracle/primarydb/password:password::"},
        {"name": "COGNITO_CLIENT_SECRET", "valueFrom": "arn:aws:secretsmanager:ap-northeast-2:YOUR_ACCOUNT_ID:secret:user-service/cognito-client-secret"}
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/user-service",
          "awslogs-region": "ap-northeast-2",
          "awslogs-stream-prefix": "ecs"
        }
      },
      "healthCheck": {
        "command": ["CMD-SHELL", "curl -f http://localhost:8082/api/users/health || exit 1"],
        "interval": 30,
        "timeout": 5,
        "retries": 3,
        "startPeriod": 60
      }
    }
  ]
}
```

#### Store Service Task Definition
```json
{
  "family": "store-service-task",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "containerDefinitions": [
    {
      "name": "store-service",
      "image": "ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/team-fog-store-service:latest",
      "portMappings": [
        {"containerPort": 8081, "protocol": "tcp"}
      ],
      "environment": [
        {"name": "SPRING_PROFILES_ACTIVE", "value": "prod"},
        {"name": "TZ", "value": "Asia/Seoul"},
        {"name": "JAVA_OPTS", "value": "-Duser.timezone=Asia/Seoul -Dfile.encoding=UTF-8"},
        {"name": "ORACLE_HOST", "value": "primarydb.internal"},
        {"name": "ORACLE_PORT", "value": "1521"},
        {"name": "ORACLE_SERVICE_NAME", "value": "TEAMFOG"},
        {"name": "ORACLE_USERNAME", "value": "store_service"},
        {"name": "USER_SERVICE_URL", "value": "http://user-service.internal:8082"},
        {"name": "RESERVATION_SERVICE_URL", "value": "http://reservation-service.internal:8080"}
      ],
      "secrets": [
        {"name": "ORACLE_PASSWORD", "valueFrom": "arn:aws:secretsmanager:ap-northeast-2:ACCOUNT_ID:secret:team-fog/oracle/primarydb/password:password::"}
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/store-service",
          "awslogs-region": "ap-northeast-2",
          "awslogs-stream-prefix": "ecs"
        }
      },
      "healthCheck": {
        "command": ["CMD-SHELL", "curl -f http://localhost:8081/actuator/health || exit 1"],
        "interval": 30,
        "timeout": 5,
        "retries": 3,
        "startPeriod": 60
      }
    }
  ]
}
```

#### Reservation Service Task Definition
```json
{
  "family": "reservation-service-task",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "containerDefinitions": [
    {
      "name": "reservation-service",
      "image": "ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/team-fog-reservation-service:latest",
      "portMappings": [
        {"containerPort": 8080, "protocol": "tcp"}
      ],
      "environment": [
        {"name": "SPRING_PROFILES_ACTIVE", "value": "prod"},
        {"name": "TZ", "value": "Asia/Seoul"},
        {"name": "JAVA_OPTS", "value": "-Duser.timezone=Asia/Seoul -Dfile.encoding=UTF-8"},
        {"name": "ORACLE_HOST", "value": "primarydb.internal"},
        {"name": "ORACLE_PORT", "value": "1521"},
        {"name": "ORACLE_SERVICE_NAME", "value": "TEAMFOG"},
        {"name": "ORACLE_USERNAME", "value": "reservation_service"},
        {"name": "USER_SERVICE_URL", "value": "http://user-service.internal:8082"},
        {"name": "STORE_SERVICE_URL", "value": "http://store-service.internal:8081"}
      ],
      "secrets": [
        {"name": "ORACLE_PASSWORD", "valueFrom": "arn:aws:secretsmanager:ap-northeast-2:ACCOUNT_ID:secret:team-fog/oracle/primarydb/password:password::"}
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/reservation-service",
          "awslogs-region": "ap-northeast-2",
          "awslogs-stream-prefix": "ecs"
        }
      },
      "healthCheck": {
        "command": ["CMD-SHELL", "curl -f http://localhost:8080/actuator/health || exit 1"],
        "interval": 30,
        "timeout": 5,
        "retries": 3,
        "startPeriod": 60
      }
    }
  ]
}
```

### 2. 배포 스크립트

#### User Service 배포
```bash
#!/bin/bash
# deploy-user-service.sh

export AWS_REGION="ap-northeast-2"
export ECR_REPOSITORY_URI="$(aws ecr describe-repositories --repository-names team-fog-user-service --query 'repositories[0].repositoryUri' --output text)"
export ECS_CLUSTER_NAME="team-fog-cluster"
export ECS_SERVICE_NAME="user-service"
export ECS_TASK_DEFINITION_NAME="user-service"

echo "🚀 User Service 배포 시작..."

# 1. Docker 이미지 빌드
docker build -t team-fog-user-service:latest .

# 2. ECR 로그인
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_REPOSITORY_URI

# 3. 이미지 태그 및 푸시
docker tag team-fog-user-service:latest $ECR_REPOSITORY_URI:latest
docker push $ECR_REPOSITORY_URI:latest

# 4. ECS Task Definition 등록
aws ecs register-task-definition --cli-input-json file://aws/ecs-task-definition.json

# 5. ECS 서비스 업데이트
aws ecs update-service \
    --cluster $ECS_CLUSTER_NAME \
    --service $ECS_SERVICE_NAME \
    --force-new-deployment

echo "✅ User Service 배포 완료!"
```

#### Store Service 배포
```bash
#!/bin/bash
# deploy-store-service.sh

export AWS_REGION="ap-northeast-2"
export ECR_REPOSITORY_URI="$(aws ecr describe-repositories --repository-names team-fog-store-service --query 'repositories[0].repositoryUri' --output text)"
export ECS_CLUSTER_NAME="team-fog-cluster"
export ECS_SERVICE_NAME="store-service"
export ECS_TASK_DEFINITION_NAME="store-service-task"

echo "🚀 Store Service 배포 시작..."

# 1. Docker 이미지 빌드
docker build -t team-fog-store-service:latest .

# 2. ECR 로그인
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_REPOSITORY_URI

# 3. 이미지 태그 및 푸시
docker tag team-fog-store-service:latest $ECR_REPOSITORY_URI:latest
docker push $ECR_REPOSITORY_URI:latest

# 4. ECS Task Definition 등록
aws ecs register-task-definition --cli-input-json file://aws/store-service-task-definition.json

# 5. ECS 서비스 업데이트
aws ecs update-service \
    --cluster $ECS_CLUSTER_NAME \
    --service $ECS_SERVICE_NAME \
    --force-new-deployment

echo "✅ Store Service 배포 완료!"
```

#### Reservation Service 배포
```bash
#!/bin/bash
# deploy-reservation-service.sh

export AWS_REGION="ap-northeast-2"
export ECR_REPOSITORY_URI="$(aws ecr describe-repositories --repository-names team-fog-reservation-service --query 'repositories[0].repositoryUri' --output text)"
export ECS_CLUSTER_NAME="team-fog-cluster"
export ECS_SERVICE_NAME="reservation-service"
export ECS_TASK_DEFINITION_NAME="reservation-service-task"

echo "🚀 Reservation Service 배포 시작..."

# 1. Docker 이미지 빌드
docker build -t team-fog-reservation-service:latest .

# 2. ECR 로그인
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_REPOSITORY_URI

# 3. 이미지 태그 및 푸시
docker tag team-fog-reservation-service:latest $ECR_REPOSITORY_URI:latest
docker push $ECR_REPOSITORY_URI:latest

# 4. ECS Task Definition 등록
aws ecs register-task-definition --cli-input-json file://aws/reservation-service-task-definition.json

# 5. ECS 서비스 업데이트
aws ecs update-service \
    --cluster $ECS_CLUSTER_NAME \
    --service $ECS_SERVICE_NAME \
    --force-new-deployment

echo "✅ Reservation Service 배포 완료!"
```

## 🔐 보안 설정

### 1. IAM 역할 및 정책

#### ECS Task Execution Role
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "ecr:GetAuthorizationToken",
        "ecr:BatchCheckLayerAvailability",
        "ecr:GetDownloadUrlForLayer",
        "ecr:BatchGetImage"
      ],
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "logs:CreateLogStream",
        "logs:PutLogEvents"
      ],
      "Resource": "arn:aws:logs:ap-northeast-2:*:log-group:/ecs/*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "secretsmanager:GetSecretValue"
      ],
      "Resource": [
        "arn:aws:secretsmanager:ap-northeast-2:*:secret:team-fog/*"
      ]
    }
  ]
}
```

#### User Service Task Role
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "cognito-idp:AdminGetUser",
        "cognito-idp:AdminInitiateAuth",
        "cognito-idp:AdminRespondToAuthChallenge"
      ],
      "Resource": "arn:aws:cognito-idp:ap-northeast-2:*:userpool/*"
    }
  ]
}
```

### 2. Secrets Manager 설정

#### Oracle Database 비밀번호
```bash
aws secretsmanager create-secret \
    --name "team-fog/oracle/primarydb/password" \
    --description "Oracle Database password for Team-FOG" \
    --secret-string "Oracle123456"
```

#### Cognito Client Secret
```bash
aws secretsmanager create-secret \
    --name "user-service/cognito-client-secret" \
    --description "AWS Cognito Client Secret for User Service" \
    --secret-string "your-cognito-client-secret"
```

## 🌐 네트워크 설정

### 1. VPC 구성
```bash
# VPC 생성
aws ec2 create-vpc \
    --cidr-block 10.0.0.0/16 \
    --tag-specifications ResourceType=vpc,Tags=[{Key=Name,Value=team-fog-vpc}]

# 서브넷 생성
aws ec2 create-subnet \
    --vpc-id vpc-xxxxxxxxx \
    --cidr-block 10.0.1.0/24 \
    --availability-zone ap-northeast-2a

aws ec2 create-subnet \
    --vpc-id vpc-xxxxxxxxx \
    --cidr-block 10.0.2.0/24 \
    --availability-zone ap-northeast-2c
```

### 2. Security Groups
```bash
# Application Load Balancer Security Group
aws ec2 create-security-group \
    --group-name team-fog-alb-sg \
    --description "Security group for ALB" \
    --vpc-id vpc-xxxxxxxxx

# ECS Services Security Group
aws ec2 create-security-group \
    --group-name team-fog-ecs-sg \
    --description "Security group for ECS services" \
    --vpc-id vpc-xxxxxxxxx

# Database Security Group
aws ec2 create-security-group \
    --group-name team-fog-db-sg \
    --description "Security group for Oracle Database" \
    --vpc-id vpc-xxxxxxxxx
```

## 📊 모니터링 및 로깅

### 1. CloudWatch 로그 그룹
```bash
# 로그 그룹 생성
aws logs create-log-group --log-group-name /ecs/user-service
aws logs create-log-group --log-group-name /ecs/store-service
aws logs create-log-group --log-group-name /ecs/reservation-service
aws logs create-log-group --log-group-name /ecs/frontend
aws logs create-log-group --log-group-name /ecs/primarydb
aws logs create-log-group --log-group-name /ecs/standbydb
```

### 2. CloudWatch 알람
```bash
# CPU 사용률 알람
aws cloudwatch put-metric-alarm \
    --alarm-name "user-service-cpu-high" \
    --alarm-description "User Service CPU usage is high" \
    --metric-name CPUUtilization \
    --namespace AWS/ECS \
    --statistic Average \
    --period 300 \
    --threshold 80 \
    --comparison-operator GreaterThanThreshold \
    --evaluation-periods 2 \
    --alarm-actions arn:aws:sns:ap-northeast-2:ACCOUNT_ID:team-fog-alerts
```

## 🔄 서비스 간 통신

### 1. 내부 통신 (Service Discovery)
```yaml
# ECS Service Discovery 설정
services:
  - name: user-service
    port: 8082
    dnsName: user-service.internal
  
  - name: store-service
    port: 8081
    dnsName: store-service.internal
  
  - name: reservation-service
    port: 8080
    dnsName: reservation-service.internal
```

### 2. 외부 통신 (API Gateway)
```yaml
# API Gateway 설정
apiGateway:
  restApi:
    name: team-fog-api
    description: Team-FOG REST API
  
  resources:
    /api/users:
      target: user-service.internal:8082
    /api/stores:
      target: store-service.internal:8081
    /api/bookings:
      target: reservation-service.internal:8080
```

## 🧪 테스트 및 검증

### 1. 서비스 헬스체크
```bash
# User Service
curl -X GET "http://user-service.internal:8082/api/users/health"

# Store Service
curl -X GET "http://store-service.internal:8081/api/stores/health"

# Reservation Service
curl -X GET "http://reservation-service.internal:8080/api/bookings/health"
```

### 2. MSA 연동 테스트
```bash
# User Service에서 Store Service 호출
curl -H "Authorization: Bearer YOUR_TOKEN" \
  "http://user-service.internal:8082/api/users/me/reviews"

# User Service에서 Reservation Service 호출
curl -H "Authorization: Bearer YOUR_TOKEN" \
  "http://user-service.internal:8082/api/users/me/bookings"
```

### 3. 데이터베이스 연결 테스트
```bash
# Oracle DB 연결 테스트
sqlplus user_service/password@primarydb.internal:1521/TEAMFOG

# 테이블 확인
SELECT table_name FROM user_tables;
```

## 🚨 문제 해결 가이드

### 1. 서비스 시작 실패
```bash
# ECS 서비스 로그 확인
aws logs describe-log-streams --log-group-name /ecs/user-service --order-by LastEventTime --descending

# 최신 로그 스트림 조회
aws logs get-log-events --log-group-name /ecs/user-service --log-stream-name ecs/user-service/xxxxxxxxx
```

### 2. 데이터베이스 연결 실패
```bash
# Oracle DB 상태 확인
aws ecs describe-services --cluster team-fog-cluster --services primarydb

# 네트워크 연결 테스트
aws ecs run-task --cluster team-fog-cluster --task-definition user-service --network-configuration "awsvpcConfiguration={subnets=[subnet-xxxxxxxxx],securityGroups=[sg-xxxxxxxxx]}"
```

### 3. MSA 연동 실패
```bash
# 서비스 디스커버리 확인
aws servicediscovery list-services

# DNS 해결 테스트
nslookup user-service.internal
nslookup store-service.internal
nslookup reservation-service.internal
```

## 📋 체크리스트

### 배포 전 체크리스트
- [ ] 모든 서비스의 Docker 이미지가 ECR에 푸시됨
- [ ] ECS Task Definitions가 올바르게 등록됨
- [ ] 환경변수와 시크릿이 올바르게 설정됨
- [ ] Security Groups가 올바르게 구성됨
- [ ] 데이터베이스가 실행 중이고 접근 가능함
- [ ] CloudWatch 로그 그룹이 생성됨

### 배포 후 체크리스트
- [ ] 모든 ECS 서비스가 RUNNING 상태임
- [ ] 헬스체크가 성공함
- [ ] 서비스 간 통신이 정상임
- [ ] 데이터베이스 연결이 정상임
- [ ] 로그가 정상적으로 수집됨
- [ ] 모니터링 알람이 설정됨

## 📞 팀별 담당자

### User Service
- **담당자**: User Service 개발팀
- **브랜치**: `user_sample`
- **배포 스크립트**: `deploy-user-service.sh`

### Store Service
- **담당자**: Store Service 개발팀
- **브랜치**: `store_sample_v2`
- **배포 스크립트**: `deploy-store-service.sh`

### Booking Service
- **담당자**: Booking Service 개발팀
- **브랜치**: `sample`
- **배포 스크립트**: `deploy-reservation-service.sh`

### Frontend
- **담당자**: Frontend 개발팀
- **브랜치**: `sample`
- **배포**: S3 + CloudFront 또는 ECS Fargate

### Database
- **담당자**: Database 관리팀
- **설정**: Oracle PDB + Standby
- **배포**: ECS EC2

---

**문서 버전**: 1.0  
**최종 업데이트**: 2025년 8월 14일  
**작성자**: Team-FOG 전체 개발팀
