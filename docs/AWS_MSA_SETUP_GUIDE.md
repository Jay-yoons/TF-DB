# 🚀 AWS MSA 세팅 가이드

## 📋 개요

Team-FOG 프로젝트의 AWS MSA 환경 구축을 위한 가이드입니다. AWS 서버 세팅 담당 팀원이 참고하여 전체 시스템을 구성할 수 있습니다.

## 🏗️ 전체 아키텍처

```
┌─────────────────────────────────────────────────────────────────┐
│                        AWS Cloud                                 │
├─────────────────────────────────────────────────────────────────┤
│  Application Load Balancer (ALB)                                │
│  Port: 80/443                                                   │
├─────────────────────────────────────────────────────────────────┤
│  ECS Cluster (Fargate)                                          │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐│
│  │ User Service│ │Store Service│ │Reservation  │ │ Frontend    ││
│  │ Port: 8082  │ │ Port: 8081  │ │Service      │ │ Port: 3000  ││
│  │             │ │             │ │ Port: 8080  │ │             ││
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘│
├─────────────────────────────────────────────────────────────────┤
│  ECS Cluster (EC2) - Oracle Database Services                   │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │ PrimaryDB (CDB + PDB)                                        ││
│  │ Port: 1521                                                  ││
│  │ - CDB: ORCL (Container Database)                            ││
│  │ - PDB: TEAMFOG (Pluggable Database)                         ││
│  │   ├── User Service Schema                                   ││
│  │   ├── Store Service Schema                                  ││
│  │   └── Reservation Service Schema                            ││
│  └─────────────────────────────────────────────────────────────┘│
│  ┌─────────────────────────────────────────────────────────────┐│
│  │ StandbyDB (CDB + PDB)                                        ││
│  │ Port: 1521                                                  ││
│  │ - CDB: ORCL (Container Database)                            ││
│  │ - PDB: TEAMFOG (Pluggable Database)                         ││
│  │   ├── User Service Schema (Read-Only)                       ││
│  │   ├── Store Service Schema (Read-Only)                      ││
│  │   └── Reservation Service Schema (Read-Only)                ││
│  └─────────────────────────────────────────────────────────────┘│
├─────────────────────────────────────────────────────────────────┤
│  AWS Cognito                                                    │
│  - User Pool                                                    │
│  - App Client                                                   │
└─────────────────────────────────────────────────────────────────┘
```

## 🔧 서비스별 포트 및 설정

### 1. User Service (포트: 8082)

**담당자**: User Service 담당자

**기능**:
- 사용자 인증 (AWS Cognito)
- 회원가입/로그인
- 마이페이지 (리뷰, 즐겨찾기)
- 사용자 정보 관리
- **Oracle Database 연동**

**환경변수**:
```bash
# 필수 환경변수
SPRING_PROFILES_ACTIVE=prod
COGNITO_USER_POOL_ID=ap-northeast-2_xxxxxxxxx
COGNITO_CLIENT_ID=xxxxxxxxxxxxxxxxxxxxxxxxxxx
COGNITO_CLIENT_SECRET=xxxxxxxxxxxxxxxxxxxxxxxxxxx
DB_HOST=user-db.cluster-xxxxx.ap-northeast-2.rds.amazonaws.com
DB_NAME=userdb
DB_USERNAME=user_service
DB_PASSWORD=xxxxxxxxxxxx

# MSA 연동
STORE_SERVICE_URL=http://store-service.internal:8081
RESERVATION_SERVICE_URL=http://reservation-service.internal:8080
FRONTEND_URL=https://team-fog-frontend.com

# Oracle Database 설정 (ECS EC2 기반 - PDB + Standby)
# PrimaryDB
ORACLE_HOST=primarydb.internal
ORACLE_PORT=1521
ORACLE_SERVICE_NAME=TEAMFOG
ORACLE_USERNAME=user_service
ORACLE_PASSWORD=xxxxxxxxxxxx
ORACLE_MAX_POOL_SIZE=30
ORACLE_MIN_POOL_SIZE=10
ORACLE_CONNECTION_TIMEOUT=60000
ORACLE_IDLE_TIMEOUT=1200000
ORACLE_SSL_ENABLED=false

# StandbyDB (Read-Only)
ORACLE_STANDBY_HOST=standbydb.internal
ORACLE_STANDBY_PORT=1521
ORACLE_STANDBY_SERVICE_NAME=TEAMFOG
ORACLE_STANDBY_USERNAME=user_service_readonly
ORACLE_STANDBY_PASSWORD=xxxxxxxxxxxx
```

**헬스체크**:
- URL: `/api/users/health`
- 예상 응답: `{"service":"user-service","status":"UP"}`

### 2. Store Service (포트: 8081)

**담당자**: Store Service 담당자

**기능**:
- 가게 정보 관리
- 리뷰 시스템
- 메뉴 관리

**환경변수**:
```bash
SPRING_PROFILES_ACTIVE=prod
DB_HOST=store-db.cluster-xxxxx.ap-northeast-2.rds.amazonaws.com
DB_NAME=storedb
DB_USERNAME=store_service
DB_PASSWORD=xxxxxxxxxxxx

# MSA 연동
USER_SERVICE_URL=http://user-service.internal:8082
RESERVATION_SERVICE_URL=http://reservation-service.internal:8080
```

**헬스체크**:
- URL: `/api/stores/health`
- 예상 응답: `{"service":"store-service","status":"UP"}`

### 3. Reservation Service (포트: 8080)

**담당자**: Reservation Service 담당자

**기능**:
- 예약 시스템
- 예약 관리
- 결제 연동

**환경변수**:
```bash
SPRING_PROFILES_ACTIVE=prod
DB_HOST=reservation-db.cluster-xxxxx.ap-northeast-2.rds.amazonaws.com
DB_NAME=reservationdb
DB_USERNAME=reservation_service
DB_PASSWORD=xxxxxxxxxxxx

# MSA 연동
USER_SERVICE_URL=http://user-service.internal:8082
STORE_SERVICE_URL=http://store-service.internal:8081
```

**헬스체크**:
- URL: `/api/reservations/health`
- 예상 응답: `{"service":"reservation-service","status":"UP"}`

### 4. Frontend (포트: 3000)

**담당자**: Frontend 담당자

**기능**:
- Vue.js 기반 사용자 인터페이스
- API Gateway 역할

**환경변수**:
```bash
NODE_ENV=production
VUE_APP_API_BASE_URL=https://api.team-fog.com
VUE_APP_COGNITO_USER_POOL_ID=ap-northeast-2_xxxxxxxxx
VUE_APP_COGNITO_CLIENT_ID=xxxxxxxxxxxxxxxxxxxxxxxxxxx
VUE_APP_COGNITO_DOMAIN=team-fog.auth.ap-northeast-2.amazoncognito.com
```

## 🗄️ 데이터베이스 설정

### Oracle Database 설정 (ECS EC2 기반 - PDB + Standby)

**Oracle DB ECS 서비스 생성**:
```bash
# PrimaryDB Task Definition 생성
aws ecs register-task-definition --cli-input-json file://aws/primarydb-task-definition.json

# StandbyDB Task Definition 생성
aws ecs register-task-definition --cli-input-json file://aws/standbydb-task-definition.json

# PrimaryDB 서비스 생성
aws ecs create-service \
  --cluster team-fog-db-cluster \
  --service-name primarydb \
  --task-definition primarydb:1 \
  --desired-count 1 \
  --launch-type EC2 \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-xxxxx,subnet-yyyyy],securityGroups=[sg-xxxxx],assignPublicIp=DISABLED}"

# StandbyDB 서비스 생성
aws ecs create-service \
  --cluster team-fog-db-cluster \
  --service-name standbydb \
  --task-definition standbydb:1 \
  --desired-count 1 \
  --launch-type EC2 \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-xxxxx,subnet-yyyyy],securityGroups=[sg-xxxxx],assignPublicIp=DISABLED}"
```

**Oracle DB 초기 설정 (CDB + PDB)**:
```sql
-- Container Database (CDB) 설정
-- PDB 생성
CREATE PLUGGABLE DATABASE TEAMFOG
ADMIN USER teamfog_admin IDENTIFIED BY "admin_password_here"
STORAGE (MAXSIZE 2G)
DEFAULT TABLESPACE teamfog_data
DATAFILE '/opt/oracle/oradata/ORCL/TEAMFOG/teamfog_data01.dbf' SIZE 100M
AUTOEXTEND ON NEXT 10M MAXSIZE 1G;

-- PDB 열기
ALTER PLUGGABLE DATABASE TEAMFOG OPEN;

-- PDB로 전환
ALTER SESSION SET CONTAINER = TEAMFOG;

-- User Service 스키마 생성
CREATE USER user_service IDENTIFIED BY "secure_password_here";

-- 권한 부여
GRANT CONNECT, RESOURCE TO user_service;
GRANT CREATE SESSION TO user_service;
GRANT CREATE TABLE TO user_service;
GRANT CREATE VIEW TO user_service;
GRANT CREATE SEQUENCE TO user_service;

-- 테이블스페이스 생성
CREATE TABLESPACE user_data
DATAFILE '/opt/oracle/oradata/ORCL/TEAMFOG/user_data01.dbf' SIZE 100M
AUTOEXTEND ON NEXT 10M MAXSIZE 1G;

-- 사용자에게 테이블스페이스 할당
ALTER USER user_service QUOTA UNLIMITED ON user_data;

-- Read-Only 사용자 생성 (Standby용)
CREATE USER user_service_readonly IDENTIFIED BY "readonly_password_here";
GRANT CONNECT TO user_service_readonly;
GRANT SELECT ANY TABLE TO user_service_readonly;
```

**User Service 테이블 생성**:
```sql
-- 사용자 테이블
CREATE TABLE users (
    user_id VARCHAR2(15) PRIMARY KEY,
    user_name VARCHAR2(20) NOT NULL,
    phone_number VARCHAR2(20) UNIQUE NOT NULL,
    user_location VARCHAR2(50),
    password VARCHAR2(255) NOT NULL,
    is_active NUMBER(1) DEFAULT 1 NOT NULL,
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- 즐겨찾기 가게 테이블
CREATE TABLE mv_fav_store (
    fav_store_id NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    user_id VARCHAR2(15) NOT NULL,
    store_id VARCHAR2(20) NOT NULL,
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT fav_store_un UNIQUE (user_id, store_id)
);

-- 인덱스 생성
CREATE INDEX idx_users_phone ON users(phone_number);
CREATE INDEX idx_fav_store_user ON mv_fav_store(user_id);
CREATE INDEX idx_fav_store_store ON mv_fav_store(store_id);
```

### Oracle PDB 스키마 설정 (PrimaryDB)

**Store Service 스키마 생성**:
```sql
-- PDB로 전환
ALTER SESSION SET CONTAINER = TEAMFOG;

-- Store Service 스키마 생성
CREATE USER store_service IDENTIFIED BY "store_password_here";

-- 권한 부여
GRANT CONNECT, RESOURCE TO store_service;
GRANT CREATE SESSION TO store_service;
GRANT CREATE TABLE TO store_service;
GRANT CREATE VIEW TO store_service;
GRANT CREATE SEQUENCE TO store_service;

-- 테이블스페이스 생성
CREATE TABLESPACE store_data
DATAFILE '/opt/oracle/oradata/ORCL/TEAMFOG/store_data01.dbf' SIZE 100M
AUTOEXTEND ON NEXT 10M MAXSIZE 1G;

-- 사용자에게 테이블스페이스 할당
ALTER USER store_service QUOTA UNLIMITED ON store_data;

-- Read-Only 사용자 생성 (StandbyDB용)
CREATE USER store_service_readonly IDENTIFIED BY "store_readonly_password_here";
GRANT CONNECT TO store_service_readonly;
GRANT SELECT ANY TABLE TO store_service_readonly;
```

**Reservation Service 스키마 생성**:
```sql
-- PDB로 전환
ALTER SESSION SET CONTAINER = TEAMFOG;

-- Reservation Service 스키마 생성
CREATE USER reservation_service IDENTIFIED BY "reservation_password_here";

-- 권한 부여
GRANT CONNECT, RESOURCE TO reservation_service;
GRANT CREATE SESSION TO reservation_service;
GRANT CREATE TABLE TO reservation_service;
GRANT CREATE VIEW TO reservation_service;
GRANT CREATE SEQUENCE TO reservation_service;

-- 테이블스페이스 생성
CREATE TABLESPACE reservation_data
DATAFILE '/opt/oracle/oradata/ORCL/TEAMFOG/reservation_data01.dbf' SIZE 100M
AUTOEXTEND ON NEXT 10M MAXSIZE 1G;

-- 사용자에게 테이블스페이스 할당
ALTER USER reservation_service QUOTA UNLIMITED ON reservation_data;

-- Read-Only 사용자 생성 (StandbyDB용)
CREATE USER reservation_service_readonly IDENTIFIED BY "reservation_readonly_password_here";
GRANT CONNECT TO reservation_service_readonly;
GRANT SELECT ANY TABLE TO reservation_service_readonly;
```

## 🔐 AWS Cognito 설정

### User Pool 생성

1. **AWS Console → Cognito → User Pools → Create user pool**

2. **기본 설정**:
   - Pool name: `team-fog-user-pool`
   - Region: `ap-northeast-2`

3. **보안 설정**:
   - Password policy: AWS defaults
   - MFA: Optional
   - User account recovery: Enabled

4. **App client 생성**:
   - App client name: `team-fog-client`
   - Client secret: Generate client secret
   - Callback URLs: `https://team-fog-frontend.com/callback`
   - Sign out URLs: `https://team-fog-frontend.com`
   - Allowed OAuth Flows: Authorization code grant
   - Allowed OAuth Scopes: openid, email, profile

### App Client 설정 예시

```json
{
  "ClientId": "xxxxxxxxxxxxxxxxxxxxxxxxxxx",
  "ClientSecret": "xxxxxxxxxxxxxxxxxxxxxxxxxxx",
  "UserPoolId": "ap-northeast-2_xxxxxxxxx",
  "AllowedOAuthFlows": ["code"],
  "AllowedOAuthScopes": ["openid", "email", "profile"],
  "CallbackURLs": ["https://team-fog-frontend.com/callback"],
  "LogoutURLs": ["https://team-fog-frontend.com"]
}
```

## 🐳 ECS 설정

### ECR 리포지토리 생성

```bash
# User Service
aws ecr create-repository --repository-name user-service --region ap-northeast-2

# Store Service
aws ecr create-repository --repository-name store-service --region ap-northeast-2

# Reservation Service
aws ecr create-repository --repository-name reservation-service --region ap-northeast-2

# Frontend
aws ecr create-repository --repository-name team-fog-frontend --region ap-northeast-2
```

### ECS 클러스터 생성

```bash
# 애플리케이션 서비스용 클러스터 (Fargate)
aws ecs create-cluster --cluster-name team-fog-cluster --region ap-northeast-2

# 데이터베이스 서비스용 클러스터 (EC2)
aws ecs create-cluster --cluster-name team-fog-db-cluster --region ap-northeast-2
```

### ECS EC2 인스턴스 설정 (데이터베이스용)

**EC2 인스턴스 타입**: `t3.medium` 이상 권장
**AMI**: Amazon Linux 2 또는 Ubuntu 20.04 LTS
**보안 그룹**: 데이터베이스 포트 허용 (1521, 5432, 6379)

```bash
# EC2 인스턴스에 ECS Agent 설치
sudo yum update -y
sudo yum install -y ecs-init
sudo systemctl enable --now ecs

# ECS 클러스터에 등록
echo 'ECS_CLUSTER=team-fog-db-cluster' >> /etc/ecs/ecs.config
sudo systemctl restart ecs
```

### Task Definition 예시 (User Service)

```json
{
  "family": "user-service",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "executionRoleArn": "arn:aws:iam::YOUR_ACCOUNT_ID:role/ecsTaskExecutionRole",
  "taskRoleArn": "arn:aws:iam::YOUR_ACCOUNT_ID:role/user-service-task-role",
  "containerDefinitions": [
    {
      "name": "user-service",
      "image": "YOUR_ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/user-service:latest",
      "portMappings": [
        {
          "containerPort": 8082,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "prod"
        }
      ],
      "secrets": [
        {
          "name": "COGNITO_CLIENT_SECRET",
          "valueFrom": "arn:aws:secretsmanager:ap-northeast-2:YOUR_ACCOUNT_ID:secret:user-service/cognito-client-secret"
        },
        {
          "name": "DB_PASSWORD",
          "valueFrom": "arn:aws:secretsmanager:ap-northeast-2:YOUR_ACCOUNT_ID:secret:user-service/db-password"
        }
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
        "command": [
          "CMD-SHELL",
          "curl -f http://localhost:8082/api/users/health || exit 1"
        ],
        "interval": 30,
        "timeout": 5,
        "retries": 3,
        "startPeriod": 60
      }
    }
  ]
}
```

## 🔒 IAM 역할 및 정책

### ECS Task Execution Role

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
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "secretsmanager:GetSecretValue"
      ],
      "Resource": [
        "arn:aws:secretsmanager:ap-northeast-2:YOUR_ACCOUNT_ID:secret:user-service/*",
        "arn:aws:secretsmanager:ap-northeast-2:YOUR_ACCOUNT_ID:secret:store-service/*",
        "arn:aws:secretsmanager:ap-northeast-2:YOUR_ACCOUNT_ID:secret:reservation-service/*"
      ]
    }
  ]
}
```

### User Service Task Role

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
      "Resource": "arn:aws:cognito-idp:ap-northeast-2:YOUR_ACCOUNT_ID:userpool/ap-northeast-2_xxxxxxxxx"
    }
  ]
}
```

## 🔐 Secrets Manager 설정

```bash
# User Service Secrets
aws secretsmanager create-secret \
  --name user-service/cognito-client-secret \
  --secret-string "your-cognito-client-secret" \
  --region ap-northeast-2

aws secretsmanager create-secret \
  --name user-service/oracle-password \
  --secret-string "your-oracle-password" \
  --region ap-northeast-2

# Database Secrets
aws secretsmanager create-secret \
  --name primarydb/password \
  --secret-string "Oracle123456" \
  --region ap-northeast-2

aws secretsmanager create-secret \
  --name standbydb/password \
  --secret-string "Oracle123456" \
  --region ap-northeast-2

# Store Service Secrets
aws secretsmanager create-secret \
  --name store-service/db-password \
  --secret-string "your-store-db-password" \
  --region ap-northeast-2

# Reservation Service Secrets
aws secretsmanager create-secret \
  --name reservation-service/db-password \
  --secret-string "your-reservation-db-password" \
  --region ap-northeast-2
```

## 🌐 Application Load Balancer 설정

### Target Groups

1. **User Service Target Group**:
   - Port: 8082
   - Protocol: HTTP
   - Health check path: `/api/users/health`

2. **Store Service Target Group**:
   - Port: 8081
   - Protocol: HTTP
   - Health check path: `/api/stores/health`

3. **Reservation Service Target Group**:
   - Port: 8080
   - Protocol: HTTP
   - Health check path: `/api/reservations/health`

4. **Frontend Target Group**:
   - Port: 3000
   - Protocol: HTTP
   - Health check path: `/`

### Listener Rules

```
Rule 1: /api/users/* → User Service Target Group
Rule 2: /api/stores/* → Store Service Target Group
Rule 3: /api/reservations/* → Reservation Service Target Group
Rule 4: /* → Frontend Target Group
```

## 📊 모니터링 설정

### CloudWatch 로그 그룹

```bash
# 애플리케이션 서비스 로그 그룹
aws logs create-log-group --log-group-name /ecs/user-service --region ap-northeast-2
aws logs create-log-group --log-group-name /ecs/store-service --region ap-northeast-2
aws logs create-log-group --log-group-name /ecs/reservation-service --region ap-northeast-2
aws logs create-log-group --log-group-name /ecs/frontend --region ap-northeast-2

# 데이터베이스 서비스 로그 그룹
aws logs create-log-group --log-group-name /ecs/primarydb --region ap-northeast-2
aws logs create-log-group --log-group-name /ecs/standbydb --region ap-northeast-2
```

### CloudWatch 대시보드

**메트릭 모니터링**:
- CPU 사용률
- 메모리 사용률
- 네트워크 I/O
- API 응답 시간
- 에러율

## 🚀 배포 스크립트

### 배포 순서

1. **RDS 데이터베이스 생성**
2. **AWS Cognito User Pool 설정**
3. **ECR 리포지토리 생성**
4. **Docker 이미지 빌드 및 푸시**
5. **ECS Task Definition 등록**
6. **ECS 서비스 생성**
7. **ALB 설정**
8. **DNS 설정**

### 배포 스크립트 예시

```bash
#!/bin/bash

# 환경변수 설정
export AWS_REGION=ap-northeast-2
export ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
export CLUSTER_NAME=team-fog-cluster

# ECR 로그인
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com

# User Service 배포
cd user-service
docker build -t user-service .
docker tag user-service:latest $ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/user-service:latest
docker push $ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/user-service:latest

# ECS 서비스 업데이트
aws ecs update-service --cluster $CLUSTER_NAME --service user-service --force-new-deployment

echo "User Service 배포 완료!"
```

## 📞 연락처

- **AWS 서버 세팅 담당자**: [담당자명]
- **User Service 담당자**: [담당자명]
- **Store Service 담당자**: [담당자명]
- **Reservation Service 담당자**: [담당자명]
- **Frontend 담당자**: [담당자명]

## 📚 추가 문서

- [User Service 가이드](../README.md)
- [배포 가이드](DEPLOYMENT_GUIDE.md)
- [MSA 연동 가이드](MSA_INTEGRATION.md)
