# 🚀 Team-FOG AWS ECS MSA 배포 - 인프라 담당자용

## 📋 **빠른 시작**

### **1. 필수 준비사항**
- AWS CLI 설치 및 설정 완료
- Docker 설치 완료
- Java 17 설치 완료

### **2. 배포 순서**
```bash
# 1. AWS 인프라 생성
chmod +x aws/setup-aws-infrastructure.sh
./aws/setup-aws-infrastructure.sh

# 2. DB 담당자에게 Oracle DB 정보 요청
# - EC2 Private IP
# - Oracle SID
# - DB 사용자명/비밀번호
# - 보안 그룹 ID

# 3. Oracle DB 시크릿 생성
aws secretsmanager create-secret \
    --name "team-fog/oracle-db-credentials" \
    --description "Oracle DB credentials for Team-FOG" \
    --secret-string '{"host":"10.0.x.x","port":"1521","sid":"ORCL","username":"teamfog_user","password":"your_password"}'

# 4. User Service 배포
chmod +x aws/deployment-scripts/deploy-user-service.sh
./aws/deployment-scripts/deploy-user-service.sh
```

### **3. 확인 사항**
```bash
# 서비스 상태 확인
aws ecs describe-services --cluster team-fog-cluster --services team-fog-user-service

# Health Check
curl -X GET http://[ALB-DNS]:8082/actuator/health
```

## 📚 **상세 가이드**
자세한 내용은 `AWS_MSA_DEPLOYMENT_GUIDE.md` 파일을 참조하세요.

## 🔧 **주요 파일**
- `aws/setup-aws-infrastructure.sh` - AWS 인프라 생성 스크립트
- `aws/deployment-scripts/deploy-user-service.sh` - User Service 배포 스크립트
- `aws/ecs-task-definition.json` - ECS Task Definition
- `Dockerfile` - Docker 이미지 설정
- `application-prod.yml` - 프로덕션 환경 설정

## 📞 **지원**
문제 발생 시 개발팀에 문의하세요.
