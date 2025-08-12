# 🚀 Team-FOG MSA AWS ECS 배포 완전 가이드

## 📋 개요

Team-FOG 프로젝트의 모든 마이크로서비스(User Service, Store Service, Reservation Service)를 AWS ECS에 배포하는 완전한 가이드입니다.

## 🏗️ 전체 아키텍처

```
┌─────────────────────────────────────────────────────────────────┐
│                        Frontend (Vue.js)                        │
│                           Port: 3000                            │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                Application Load Balancer (ALB)                  │
│                           Port: 80/443                          │
└─────────────────────────────────────────────────────────────────┘
                                │
                ┌───────────────┼───────────────┐
                ▼               ▼               ▼
┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│ User Service│ │Store Service│ │Reservation  │ │ Oracle DB   │
│ Port: 8082  │ │ Port: 8081  │ │Service      │ │ Port: 1521  │
│ ECS Fargate │ │ ECS Fargate │ │ Port: 8080  │ │ ECS Fargate │
│             │ │             │ │ ECS Fargate │ │             │
└─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘
       │               │               │               │
       └───────────────┼───────────────┼───────────────┘
                       ▼               ▼
              ┌─────────────┐ ┌─────────────┐
              │ PrimaryDB   │ │ StandbyDB   │
              │ (Oracle PDB)│ │ (Oracle PDB)│
              └─────────────┘ └─────────────┘
```

## 🔧 1단계: 사전 준비 작업

### 1.1 팀원별 필수 준비사항

**각 팀원이 완료해야 할 작업:**

#### User Service 담당자 (현재 완료됨)
- ✅ Dockerfile 생성
- ✅ ECS Task Definition 준비
- ✅ 배포 스크립트 준비
- ✅ 실제 배포환경 설정 가이드 준비

#### Store Service 담당자
```dockerfile
# Store Service Dockerfile
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8081
CMD ["java", "-jar", "app.jar"]
```

#### Reservation Service 담당자
```dockerfile
# Reservation Service Dockerfile
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
```

### 1.2 AWS 계정 및 권한 설정

```bash
# AWS CLI 설치 및 설정
aws configure
# AWS Access Key ID, Secret Access Key, Region 입력

# AWS 계정 ID 확인
aws sts get-caller-identity --query Account --output text
```

## 🔧 2단계: AWS 인프라 설정

### 2.1 인프라 설정 스크립트 실행

```bash
# 인프라 설정 가이드 참조
# aws/infrastructure-setup.md 파일을 따라 진행

# 1. VPC 및 서브넷 생성
# 2. 보안 그룹 설정
# 3. ECS 클러스터 생성
# 4. ECR 리포지토리 생성
# 5. IAM 역할 생성
# 6. Secrets Manager 설정
```

### 2.2 Oracle DB ECS 서비스 배포

```bash
# Oracle PrimaryDB 배포
aws ecs create-service \
    --cluster team-fog-cluster \
    --service-name primarydb \
    --task-definition primarydb-task \
    --desired-count 1 \
    --launch-type FARGATE \
    --network-configuration "awsvpcConfiguration={subnets=[subnet-xxxxx,subnet-yyyyy],securityGroups=[sg-xxxxx],assignPublicIp=ENABLED}"

# Oracle StandbyDB 배포
aws ecs create-service \
    --cluster team-fog-cluster \
    --service-name standbydb \
    --task-definition standbydb-task \
    --desired-count 1 \
    --launch-type FARGATE \
    --network-configuration "awsvpcConfiguration={subnets=[subnet-xxxxx,subnet-yyyyy],securityGroups=[sg-xxxxx],assignPublicIp=ENABLED}"
```

## 🔧 3단계: 마이크로서비스 배포

### 3.1 User Service 배포

```bash
# User Service 디렉토리로 이동
cd user-service-directory

# 배포 스크립트 실행
chmod +x aws/deployment-scripts/deploy-user-service.sh
./aws/deployment-scripts/deploy-user-service.sh
```

### 3.2 Store Service 배포

```bash
# Store Service 담당자가 실행
cd store-service-directory

# 배포 스크립트 실행
chmod +x aws/deployment-scripts/deploy-store-service.sh
./aws/deployment-scripts/deploy-store-service.sh
```

### 3.3 Reservation Service 배포

```bash
# Reservation Service 담당자가 실행
cd reservation-service-directory

# 배포 스크립트 실행
chmod +x aws/deployment-scripts/deploy-reservation-service.sh
./aws/deployment-scripts/deploy-reservation-service.sh
```

## 🔧 4단계: Application Load Balancer 설정

### 4.1 ALB 생성

```bash
# ALB 생성
aws elbv2 create-load-balancer \
    --name team-fog-alb \
    --subnets subnet-xxxxx subnet-yyyyy \
    --security-groups sg-xxxxx \
    --scheme internet-facing \
    --type application \
    --ip-address-type ipv4
```

### 4.2 Target Group 생성

```bash
# User Service Target Group
aws elbv2 create-target-group \
    --name user-service-tg \
    --protocol HTTP \
    --port 8082 \
    --vpc-id vpc-xxxxx \
    --target-type ip \
    --health-check-path /actuator/health

# Store Service Target Group
aws elbv2 create-target-group \
    --name store-service-tg \
    --protocol HTTP \
    --port 8081 \
    --vpc-id vpc-xxxxx \
    --target-type ip \
    --health-check-path /actuator/health

# Reservation Service Target Group
aws elbv2 create-target-group \
    --name reservation-service-tg \
    --protocol HTTP \
    --port 8080 \
    --vpc-id vpc-xxxxx \
    --target-type ip \
    --health-check-path /actuator/health
```

### 4.3 Listener 및 라우팅 규칙 설정

```bash
# ALB Listener 생성
aws elbv2 create-listener \
    --load-balancer-arn arn:aws:elasticloadbalancing:ap-northeast-2:ACCOUNT_ID:loadbalancer/app/team-fog-alb/xxxxx \
    --protocol HTTP \
    --port 80 \
    --default-actions Type=forward,TargetGroupArn=arn:aws:elasticloadbalancing:ap-northeast-2:ACCOUNT_ID:targetgroup/user-service-tg/xxxxx

# 라우팅 규칙 추가
aws elbv2 create-rule \
    --listener-arn arn:aws:elasticloadbalancing:ap-northeast-2:ACCOUNT_ID:listener/app/team-fog-alb/xxxxx/xxxxx \
    --priority 10 \
    --conditions Field=path-pattern,Values=/api/stores/* \
    --actions Type=forward,TargetGroupArn=arn:aws:elasticloadbalancing:ap-northeast-2:ACCOUNT_ID:targetgroup/store-service-tg/xxxxx

aws elbv2 create-rule \
    --listener-arn arn:aws:elasticloadbalancing:ap-northeast-2:ACCOUNT_ID:listener/app/team-fog-alb/xxxxx/xxxxx \
    --priority 20 \
    --conditions Field=path-pattern,Values=/api/reservations/* \
    --actions Type=forward,TargetGroupArn=arn:aws:elasticloadbalancing:ap-northeast-2:ACCOUNT_ID:targetgroup/reservation-service-tg/xxxxx
```

## 🔧 5단계: 서비스 연동 및 테스트

### 5.1 서비스 상태 확인

```bash
# ECS 서비스 상태 확인
aws ecs describe-services \
    --cluster team-fog-cluster \
    --services user-service store-service reservation-service primarydb standbydb

# ALB 상태 확인
aws elbv2 describe-load-balancers \
    --names team-fog-alb

# Target Group 상태 확인
aws elbv2 describe-target-health \
    --target-group-arn arn:aws:elasticloadbalancing:ap-northeast-2:ACCOUNT_ID:targetgroup/user-service-tg/xxxxx
```

### 5.2 API 테스트

```bash
# User Service 테스트
curl -X GET http://team-fog-alb-xxxxx.ap-northeast-2.elb.amazonaws.com/api/users/health

# Store Service 테스트
curl -X GET http://team-fog-alb-xxxxx.ap-northeast-2.elb.amazonaws.com/api/stores/health

# Reservation Service 테스트
curl -X GET http://team-fog-alb-xxxxx.ap-northeast-2.elb.amazonaws.com/api/reservations/health
```

### 5.3 서비스 간 연동 테스트

```bash
# User Service에서 Store Service 호출 테스트
curl -X GET http://team-fog-alb-xxxxx.ap-northeast-2.elb.amazonaws.com/api/users/me/reviews

# Store Service에서 User Service 호출 테스트
curl -X GET http://team-fog-alb-xxxxx.ap-northeast-2.elb.amazonaws.com/api/stores/reviews/user/user123
```

## 🔧 6단계: 모니터링 및 로깅 설정

### 6.1 CloudWatch 로그 확인

```bash
# User Service 로그 확인
aws logs tail /ecs/user-service --follow

# Store Service 로그 확인
aws logs tail /ecs/store-service --follow

# Reservation Service 로그 확인
aws logs tail /ecs/reservation-service --follow
```

### 6.2 CloudWatch 메트릭 설정

```bash
# ECS 서비스 메트릭 확인
aws cloudwatch get-metric-statistics \
    --namespace AWS/ECS \
    --metric-name CPUUtilization \
    --dimensions Name=ServiceName,Value=user-service Name=ClusterName,Value=team-fog-cluster \
    --start-time 2024-01-01T00:00:00Z \
    --end-time 2024-01-01T23:59:59Z \
    --period 300 \
    --statistics Average
```

## 🔧 7단계: 도메인 및 SSL 설정

### 7.1 Route 53 도메인 설정

```bash
# 도메인 생성 (예: team-fog.com)
aws route53 create-hosted-zone \
    --name team-fog.com \
    --caller-reference $(date +%s)

# ALB에 도메인 연결
aws route53 change-resource-record-sets \
    --hosted-zone-id Z1234567890 \
    --change-batch file://route53-changes.json
```

### 7.2 SSL 인증서 설정

```bash
# ACM 인증서 요청
aws acm request-certificate \
    --domain-name team-fog.com \
    --subject-alternative-names *.team-fog.com \
    --validation-method DNS
```

## 📋 팀원별 작업 체크리스트

### User Service 담당자
- [ ] 실제 배포환경 설정 적용
- [ ] Oracle DB 의존성 활성화
- [ ] Docker 이미지 빌드 및 테스트
- [ ] ECR 푸시
- [ ] ECS 서비스 배포
- [ ] 서비스 상태 확인

### Store Service 담당자
- [ ] Dockerfile 생성
- [ ] 실제 배포환경 설정 적용
- [ ] Oracle DB 의존성 추가
- [ ] Docker 이미지 빌드 및 테스트
- [ ] ECR 푸시
- [ ] ECS 서비스 배포
- [ ] 서비스 상태 확인

### Reservation Service 담당자
- [ ] Dockerfile 생성
- [ ] 실제 배포환경 설정 적용
- [ ] Oracle DB 의존성 추가
- [ ] Docker 이미지 빌드 및 테스트
- [ ] ECR 푸시
- [ ] ECS 서비스 배포
- [ ] 서비스 상태 확인

### DevOps 담당자
- [ ] AWS 인프라 설정
- [ ] Oracle DB ECS 서비스 배포
- [ ] ALB 설정
- [ ] 서비스 연동 테스트
- [ ] 모니터링 설정
- [ ] 도메인 및 SSL 설정

## 🚨 문제 해결

### 일반적인 문제들

#### 1. ECS 서비스 시작 실패
```bash
# 서비스 이벤트 확인
aws ecs describe-services \
    --cluster team-fog-cluster \
    --services user-service \
    --query 'services[0].events'

# 태스크 로그 확인
aws logs tail /ecs/user-service --follow
```

#### 2. 서비스 간 통신 실패
```bash
# 보안 그룹 설정 확인
aws ec2 describe-security-groups \
    --group-ids sg-xxxxx

# 네트워크 연결 테스트
aws ecs run-task \
    --cluster team-fog-cluster \
    --task-definition user-service-task \
    --network-configuration "awsvpcConfiguration={subnets=[subnet-xxxxx],securityGroups=[sg-xxxxx],assignPublicIp=ENABLED}"
```

#### 3. 데이터베이스 연결 실패
```bash
# Oracle DB 서비스 상태 확인
aws ecs describe-services \
    --cluster team-fog-cluster \
    --services primarydb standbydb

# 데이터베이스 로그 확인
aws logs tail /ecs/primarydb --follow
```

## 📞 지원

문제가 발생하면 다음 연락처로 문의하세요:
- **User Service 담당자**: [담당자명]
- **Store Service 담당자**: [담당자명]
- **Reservation Service 담당자**: [담당자명]
- **DevOps 담당자**: [담당자명]
- **AWS 관리자**: [담당자명]

## 📚 추가 문서

- [인프라 설정 가이드](infrastructure-setup.md)
- [User Service 배포 가이드](../docs/PRODUCTION_DEPLOYMENT_GUIDE.md)
- [API 문서](../docs/API_DOCUMENTATION.md)
- [MSA 연동 가이드](../docs/MSA_INTEGRATION.md)
