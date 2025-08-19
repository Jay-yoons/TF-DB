# 🚀 Team-FOG AWS ECS MSA 배포 가이드

## 📋 **개요**
Team-FOG 프로젝트를 AWS ECS를 사용하여 MSA(Microservice Architecture) 구조로 배포하는 가이드입니다.

### **배포 아키텍처**
- **User Service**: 사용자 관리, 인증 (포트 8082)
- **Reservation Service**: 예약 관리 (포트 8080) - 추후 구현
- **Store Service**: 매장 관리 (포트 8081) - 추후 구현
- **Database**: EC2 Oracle DB (기존 인프라 활용)

## 🛠️ **사전 준비사항**

### **1. AWS CLI 설치 및 설정**
```bash
# AWS CLI 설치 (Windows)
# https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2-windows.html

# AWS CLI 설정
aws configure
# AWS Access Key ID: [입력]
# AWS Secret Access Key: [입력]
# Default region name: ap-northeast-2
# Default output format: json
```

### **2. 로컬 환경 확인**
```bash
# Docker 설치 확인
docker --version

# Java 17 설치 확인
java -version

# Gradle 설치 확인
./gradlew --version
```

## 🏗️ **1단계: AWS 인프라 생성**

### **인프라 생성 스크립트 실행**
```bash
# 스크립트 실행 권한 부여
chmod +x aws/setup-aws-infrastructure.sh

# AWS 인프라 생성 실행
./aws/setup-aws-infrastructure.sh
```

**생성되는 리소스:**
- ✅ VPC 및 서브넷 (10.0.0.0/16)
- ✅ ECS 클러스터 (team-fog-cluster)
- ✅ ECR 저장소 (3개)
- ✅ Cognito 사용자 풀
- ✅ IAM 역할
- ✅ Secrets Manager

## 🗄️ **2단계: Oracle DB 연결 설정**

### **DB 담당자에게 요청할 정보**
다음 정보를 DB 담당자에게 요청하세요:

```bash
# 필수 정보
EC2_PRIVATE_IP=10.0.x.x
ORACLE_PORT=1521
ORACLE_SID=ORCL
DB_USERNAME=teamfog_user
DB_PASSWORD=your_secure_password
EC2_SECURITY_GROUP_ID=sg-xxxxxxxxx
```

### **Oracle DB 시크릿 생성**
```bash
# Oracle DB 시크릿 생성
aws secretsmanager create-secret \
    --name "team-fog/oracle-db-credentials" \
    --description "Oracle DB credentials for Team-FOG" \
    --secret-string '{
        "host": "10.0.x.x",
        "port": "1521",
        "sid": "ORCL",
        "username": "teamfog_user",
        "password": "your_secure_password",
        "jdbcUrl": "jdbc:oracle:thin:@10.0.x.x:1521:ORCL"
    }'
```

### **보안 그룹 설정**
```bash
# EC2 Oracle DB 보안 그룹에 ECS 접근 허용
aws ec2 authorize-security-group-ingress \
    --group-id sg-xxxxxxxxx \
    --protocol tcp \
    --port 1521 \
    --source-group sg-xxxxxxxxx \
    --description "Oracle DB access from ECS tasks"
```

## 🐳 **3단계: User Service 배포**

### **배포 스크립트 실행**
```bash
# 배포 스크립트 실행 권한 부여
chmod +x aws/deployment-scripts/deploy-user-service.sh

# User Service 배포
./aws/deployment-scripts/deploy-user-service.sh
```

**배포 과정:**
1. ✅ Gradle 빌드
2. ✅ Docker 이미지 빌드
3. ✅ ECR에 이미지 푸시
4. ✅ ECS Task Definition 업데이트
5. ✅ ECS Service 배포
6. ✅ Health Check 확인

## 🌐 **4단계: 서비스 확인**

### **배포 상태 확인**
```bash
# ECS 서비스 상태 확인
aws ecs describe-services \
  --cluster team-fog-cluster \
  --services team-fog-user-service

# 로그 확인
aws logs describe-log-groups --log-group-name-prefix "/ecs/team-fog"
```

### **서비스 접속 테스트**
```bash
# Health Check
curl -X GET http://[ALB-DNS]:8082/actuator/health

# 사용자 수 조회
curl -X GET http://[ALB-DNS]:8082/api/users/count
```

## 📊 **5단계: 모니터링 설정**

### **CloudWatch 대시보드 생성**
```bash
# 대시보드 생성
aws cloudwatch put-dashboard \
  --dashboard-name "Team-FOG-MSA-Dashboard" \
  --dashboard-body file://aws/cloudwatch-dashboard.json
```

### **알람 설정**
```bash
# CPU 사용률 알람
aws cloudwatch put-metric-alarm \
  --alarm-name "Team-FOG-CPU-High" \
  --alarm-description "CPU 사용률이 70%를 초과할 때" \
  --metric-name CPUUtilization \
  --namespace AWS/ECS \
  --statistic Average \
  --period 300 \
  --threshold 70 \
  --comparison-operator GreaterThanThreshold
```

## 🔐 **6단계: 보안 강화**

### **HTTPS 설정 (선택사항)**
```bash
# SSL 인증서 요청
aws acm request-certificate \
  --domain-name api.team-fog.com \
  --validation-method DNS
```

### **API Gateway 설정 (선택사항)**
```bash
# API Gateway 생성
aws apigateway create-rest-api \
  --name "Team-FOG-API" \
  --description "Team-FOG MSA API Gateway"
```

## 🔄 **7단계: CI/CD 파이프라인 (선택사항)**

### **GitHub Actions 설정**
```yaml
# .github/workflows/deploy.yml
name: Deploy to AWS ECS
on:
  push:
    branches: [main]
jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v1
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: ap-northeast-2
      - name: Deploy to ECS
        run: |
          chmod +x aws/deployment-scripts/deploy-user-service.sh
          ./aws/deployment-scripts/deploy-user-service.sh
```

## 🔍 **문제 해결**

### **일반적인 문제들**

#### **1. ECS 서비스가 시작되지 않는 경우**
```bash
# 서비스 이벤트 확인
aws ecs describe-services \
  --cluster team-fog-cluster \
  --services team-fog-user-service \
  --query 'services[0].events'

# 태스크 로그 확인
aws logs get-log-events \
  --log-group-name "/ecs/team-fog-user-service" \
  --log-stream-name "ecs/user-service/[TASK-ID]"
```

#### **2. Oracle DB 연결 실패**
```bash
# Oracle DB 연결 테스트
telnet 10.0.x.x 1521

# 보안 그룹 확인
aws ec2 describe-security-groups --group-ids sg-xxxxxxxxx
```

#### **3. Docker 이미지 빌드 실패**
```bash
# 로컬에서 Docker 빌드 테스트
docker build -t team-fog-user-service:test .

# 빌드 로그 확인
docker build -t team-fog-user-service:test . 2>&1 | tee build.log
```

## 📈 **성능 최적화**

### **Auto Scaling 설정**
```bash
# Application Auto Scaling 설정
aws application-autoscaling register-scalable-target \
  --service-namespace ecs \
  --scalable-dimension ecs:service:DesiredCount \
  --resource-id service/team-fog-cluster/team-fog-user-service \
  --min-capacity 1 \
  --max-capacity 5
```

### **Load Balancer 설정**
```bash
# Application Load Balancer 생성
aws elbv2 create-load-balancer \
  --name team-fog-alb \
  --subnets subnet-xxxxx subnet-yyyyy \
  --security-groups sg-xxxxx
```

## ✅ **배포 완료 체크리스트**

### **인프라 준비**
- [ ] AWS CLI 설정 완료
- [ ] VPC 및 서브넷 생성
- [ ] ECS 클러스터 생성
- [ ] ECR 저장소 생성
- [ ] Cognito 사용자 풀 생성
- [ ] IAM 역할 생성

### **데이터베이스 준비**
- [ ] Oracle DB 연결 정보 수신
- [ ] Secrets Manager에 DB 정보 저장
- [ ] 보안 그룹 설정 완료
- [ ] 연결 테스트 성공

### **애플리케이션 배포**
- [ ] User Service 배포 완료
- [ ] Health Check 통과
- [ ] API 테스트 성공
- [ ] 로그 모니터링 설정

### **모니터링 및 보안**
- [ ] CloudWatch 대시보드 생성
- [ ] 알람 설정 완료
- [ ] 로그 수집 확인
- [ ] 보안 설정 검토

## 📞 **지원 및 문의**

### **유용한 명령어들**
```bash
# 전체 리소스 상태 확인
aws resourcegroupstaggingapi get-resources --tag-filters Key=Project,Values=team-fog

# 비용 확인
aws ce get-cost-and-usage \
  --time-period Start=2024-01-01,End=2024-01-31 \
  --granularity MONTHLY \
  --metrics BlendedCost

# 로그 그룹 목록
aws logs describe-log-groups --log-group-name-prefix "/ecs/team-fog"
```

### **다음 단계**
1. ✅ User Service 배포 완료
2. 🔄 Reservation Service 구현 및 배포
3. 🔄 Store Service 구현 및 배포
4. 🔄 API Gateway 설정
5. 🔄 모니터링 대시보드 완성
6. 🔄 CI/CD 파이프라인 구축

---

**🎉 축하합니다! Team-FOG MSA 배포가 완료되었습니다!**

**📧 문의사항**: 인프라 담당자에게 문의하세요.
