# 🚀 Team-FOG MSA AWS ECS 배포 작업 과정

## 📋 전체 작업 흐름

```
1️⃣ 사전 준비 (모든 팀원)
   ↓
2️⃣ AWS 인프라 설정 (DevOps 담당자)
   ↓
3️⃣ Oracle DB 배포 (DevOps 담당자)
   ↓
4️⃣ 각 서비스 배포 (각 팀원별)
   ↓
5️⃣ ALB 설정 (DevOps 담당자)
   ↓
6️⃣ 서비스 연동 테스트 (모든 팀원)
   ↓
7️⃣ 모니터링 설정 (DevOps 담당자)
```

## 📁 1️⃣ 사전 준비 단계 (모든 팀원)

### 1.1 AWS CLI 설정
```bash
# AWS CLI 설치 및 설정
aws configure
# AWS Access Key ID, Secret Access Key, Region 입력

# AWS 계정 ID 확인
aws sts get-caller-identity --query Account --output text
```

### 1.2 Docker 설치 확인
```bash
# Docker 설치 확인
docker --version
docker-compose --version
```

### 1.3 프로젝트 준비
```bash
# 각자 담당 서비스 디렉토리에서
# 1. 실제 배포환경 설정 적용
# 2. Oracle DB 의존성 활성화
# 3. Dockerfile 생성
```

## 📁 2️⃣ AWS 인프라 설정 (DevOps 담당자)

### 2.1 VPC 및 네트워크 설정
```bash
# aws/infrastructure-setup.md 파일 참조
# 1. VPC 생성
# 2. 서브넷 생성 (퍼블릭/프라이빗)
# 3. 인터넷 게이트웨이 및 NAT 게이트웨이 설정
```

### 2.2 보안 그룹 설정
```bash
# 1. ALB 보안 그룹 생성
# 2. ECS 서비스 보안 그룹 생성
# 3. Oracle DB 보안 그룹 생성
```

### 2.3 ECS 클러스터 및 ECR 설정
```bash
# 1. ECS 클러스터 생성
aws ecs create-cluster --cluster-name team-fog-cluster

# 2. ECR 리포지토리 생성
aws ecr create-repository --repository-name team-fog-user-service
aws ecr create-repository --repository-name team-fog-store-service
aws ecr create-repository --repository-name team-fog-reservation-service
```

### 2.4 IAM 역할 및 Secrets Manager 설정
```bash
# 1. ECS Task Execution Role 생성
# 2. ECS Task Role 생성
# 3. Oracle DB 비밀번호 저장
# 4. AWS Cognito 설정 저장
```

## 📁 3️⃣ Oracle DB 배포 (DevOps 담당자)

### 3.1 Oracle DB Task Definition 등록
```bash
# PrimaryDB Task Definition 등록
aws ecs register-task-definition --cli-input-json file://aws/primarydb-task-definition.json

# StandbyDB Task Definition 등록
aws ecs register-task-definition --cli-input-json file://aws/standbydb-task-definition.json
```

### 3.2 Oracle DB ECS 서비스 생성
```bash
# PrimaryDB 서비스 생성
aws ecs create-service \
    --cluster team-fog-cluster \
    --service-name primarydb \
    --task-definition primarydb-task \
    --desired-count 1 \
    --launch-type FARGATE \
    --network-configuration "awsvpcConfiguration={subnets=[subnet-xxxxx,subnet-yyyyy],securityGroups=[sg-xxxxx],assignPublicIp=ENABLED}"

# StandbyDB 서비스 생성
aws ecs create-service \
    --cluster team-fog-cluster \
    --service-name standbydb \
    --task-definition standbydb-task \
    --desired-count 1 \
    --launch-type FARGATE \
    --network-configuration "awsvpcConfiguration={subnets=[subnet-xxxxx,subnet-yyyyy],securityGroups=[sg-xxxxx],assignPublicIp=ENABLED}"
```

### 3.3 Oracle DB 상태 확인
```bash
# Oracle DB 서비스 상태 확인
aws ecs describe-services \
    --cluster team-fog-cluster \
    --services primarydb standbydb

# Oracle DB 로그 확인
aws logs tail /ecs/primarydb --follow
aws logs tail /ecs/standbydb --follow
```

## 📁 4️⃣ 각 서비스 배포 (각 팀원별)

### 4.1 User Service 배포 (User Service 담당자)

#### 4.1.1 실제 배포환경 설정 적용
```bash
# 1. build.gradle에서 Oracle DB 의존성 주석 해제
# 2. application.yml에서 Oracle DB 설정 주석 해제
# 3. Oracle 설정 클래스 주석 해제
# 4. aws.cognito.dummy-mode를 false로 변경
```

#### 4.1.2 Docker 이미지 빌드 및 배포
```bash
# User Service 디렉토리에서
chmod +x aws/deployment-scripts/deploy-user-service.sh
./aws/deployment-scripts/deploy-user-service.sh
```

### 4.2 Store Service 배포 (Store Service 담당자)

#### 4.2.1 Dockerfile 생성
```dockerfile
# Store Service 루트 디렉토리에 Dockerfile 생성
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8081
CMD ["java", "-jar", "app.jar"]
```

#### 4.2.2 실제 배포환경 설정 적용
```bash
# 1. Oracle DB 의존성 추가
# 2. application.yml 설정
# 3. 환경변수 설정
```

#### 4.2.3 Docker 이미지 빌드 및 배포
```bash
# Store Service 디렉토리에서
chmod +x aws/deployment-scripts/deploy-store-service.sh
./aws/deployment-scripts/deploy-store-service.sh
```

### 4.3 Reservation Service 배포 (Reservation Service 담당자)

#### 4.3.1 Dockerfile 생성
```dockerfile
# Reservation Service 루트 디렉토리에 Dockerfile 생성
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
```

#### 4.3.2 실제 배포환경 설정 적용
```bash
# 1. Oracle DB 의존성 추가
# 2. application.yml 설정
# 3. 환경변수 설정
```

#### 4.3.3 Docker 이미지 빌드 및 배포
```bash
# Reservation Service 디렉토리에서
chmod +x aws/deployment-scripts/deploy-reservation-service.sh
./aws/deployment-scripts/deploy-reservation-service.sh
```

## 📁 5️⃣ ALB 설정 (DevOps 담당자)

### 5.1 Application Load Balancer 생성
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

### 5.2 Target Group 생성
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

### 5.3 Listener 및 라우팅 규칙 설정
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

## 📁 6️⃣ 서비스 연동 테스트 (모든 팀원)

### 6.1 서비스 상태 확인
```bash
# ECS 서비스 상태 확인
aws ecs describe-services \
    --cluster team-fog-cluster \
    --services user-service store-service reservation-service primarydb standbydb

# ALB 상태 확인
aws elbv2 describe-load-balancers \
    --names team-fog-alb
```

### 6.2 API 테스트
```bash
# User Service 테스트
curl -X GET http://team-fog-alb-xxxxx.ap-northeast-2.elb.amazonaws.com/api/users/health

# Store Service 테스트
curl -X GET http://team-fog-alb-xxxxx.ap-northeast-2.elb.amazonaws.com/api/stores/health

# Reservation Service 테스트
curl -X GET http://team-fog-alb-xxxxx.ap-northeast-2.elb.amazonaws.com/api/reservations/health
```

### 6.3 서비스 간 연동 테스트
```bash
# User Service에서 Store Service 호출 테스트
curl -X GET http://team-fog-alb-xxxxx.ap-northeast-2.elb.amazonaws.com/api/users/me/reviews

# Store Service에서 User Service 호출 테스트
curl -X GET http://team-fog-alb-xxxxx.ap-northeast-2.elb.amazonaws.com/api/stores/reviews/user/user123
```

## 📁 7️⃣ 모니터링 설정 (DevOps 담당자)

### 7.1 CloudWatch 로그 설정
```bash
# CloudWatch 로그 그룹 생성
aws logs create-log-group --log-group-name /ecs/user-service
aws logs create-log-group --log-group-name /ecs/store-service
aws logs create-log-group --log-group-name /ecs/reservation-service
aws logs create-log-group --log-group-name /ecs/primarydb
aws logs create-log-group --log-group-name /ecs/standbydb
```

### 7.2 CloudWatch 대시보드 생성
```bash
# CloudWatch 대시보드 생성
aws cloudwatch put-dashboard \
    --dashboard-name Team-FOG-MSA-Dashboard \
    --dashboard-body file://aws/cloudwatch-dashboard.json
```

### 7.3 알람 설정
```bash
# CPU 사용률 알람
aws cloudwatch put-metric-alarm \
    --alarm-name user-service-cpu-high \
    --alarm-description "User Service CPU usage is high" \
    --metric-name CPUUtilization \
    --namespace AWS/ECS \
    --statistic Average \
    --period 300 \
    --threshold 80 \
    --comparison-operator GreaterThanThreshold \
    --evaluation-periods 2 \
    --dimensions Name=ServiceName,Value=user-service Name=ClusterName,Value=team-fog-cluster
```

## 📋 팀원별 작업 체크리스트

### DevOps 담당자
- [ ] AWS 인프라 설정 (VPC, 서브넷, 보안 그룹)
- [ ] ECS 클러스터 및 ECR 리포지토리 생성
- [ ] IAM 역할 및 Secrets Manager 설정
- [ ] Oracle DB ECS 서비스 배포
- [ ] ALB 설정
- [ ] 모니터링 설정

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

## 📚 참조 문서

- [인프라 설정 가이드](infrastructure-setup.md)
- [완전 배포 가이드](COMPLETE_DEPLOYMENT_GUIDE.md)
- [User Service 배포 가이드](../docs/PRODUCTION_DEPLOYMENT_GUIDE.md)
- [API 문서](../docs/API_DOCUMENTATION.md)
- [MSA 연동 가이드](../docs/MSA_INTEGRATION.md)
