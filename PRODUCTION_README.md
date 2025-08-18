# Team-FOG User Service - Production Deployment

## 🚀 프로덕션 배포 가이드

### 📋 개요
Team-FOG User Service의 프로덕션 배포를 위한 가이드입니다.

### 🏗️ 아키텍처
- **Spring Boot 3.5.4** + **Java 17**
- **AWS ECS** + **Fargate**
- **Oracle Database** (EC2)
- **AWS Cognito** (인증)
- **MSA 아키텍처**

### 📦 필수 구성 요소

#### 1. Oracle Database EC2 설정
```bash
# EC2 인스턴스 사양 권장
- Instance Type: t3.medium 이상
- Storage: 100GB 이상 (SSD)
- Security Group: 1521 포트 허용 (Oracle)
- VPC: Private Subnet 권장

# Oracle Database 설치 및 설정
- Oracle Database 19c 이상
- Listener 설정 (1521 포트)
- Service Name 설정
- 사용자 계정 생성 및 권한 부여
```

#### 2. 환경 변수 설정
```bash
# Oracle Database (EC2)
ORACLE_HOST=your-ec2-oracle-instance-ip
ORACLE_PORT=1521
ORACLE_SERVICE_NAME=your-service-name
ORACLE_USERNAME=your-username
ORACLE_PASSWORD=your-password

# AWS Cognito
COGNITO_USER_POOL_ID=ap-northeast-2_bdkXgjghs
COGNITO_CLIENT_ID=2gjbllg398pvoe07n4oo39nvrb
COGNITO_CLIENT_SECRET=your-client-secret
COGNITO_DOMAIN=https://ap-northeast-2bdkxgjghs.auth.ap-northeast-2.amazoncognito.com

# MSA Service URLs
RESERVATION_SERVICE_URL=http://reservation-service.internal:8080
STORE_SERVICE_URL=http://store-service.internal:8081

# Frontend URL
FRONTEND_URL=https://team-fog-frontend.com
```

#### 3. Docker 이미지 빌드
```bash
docker build -t team-fog-user-service:latest .
```

#### 4. AWS ECS 배포
```bash
# ECS Task Definition 업데이트
aws ecs register-task-definition --cli-input-json file://aws/user-service-task-definition.json

# ECS Service 업데이트
aws ecs update-service --cluster team-fog-cluster --service user-service --task-definition user-service:latest
```

### 🔧 주요 기능

#### 인증 시스템
- **AWS Cognito OAuth2** 인증
- **JWT 토큰** 검증
- **자동 사용자 생성** (Cognito → Database)

#### 사용자 관리
- 사용자 정보 CRUD
- 즐겨찾기 가게 관리
- 마이페이지 통합 기능

#### MSA 연동
- Store Service 연동
- Reservation Service 연동
- 서비스 간 통신

### 📊 모니터링

#### 헬스체크
```bash
GET /api/users/health
```

#### 메트릭스
- Spring Boot Actuator 활성화
- AWS CloudWatch 연동

### 🔒 보안

#### 네트워크 설정
```bash
# ECS → Oracle EC2 연결
- VPC: 동일 VPC 사용
- Security Group: ECS에서 Oracle EC2 1521 포트 접근 허용
- Route Table: Private Subnet 간 라우팅 설정

# Oracle EC2 보안 그룹 설정
- Inbound: 1521 (Oracle) - ECS Security Group에서만 허용
- Outbound: All Traffic 허용
```

#### CORS 설정
```yaml
cors:
  allowed-origins:
    - "https://team-fog-frontend.com"
  allowed-methods:
    - GET, POST, PUT, DELETE, OPTIONS
```

#### JWT 토큰 검증
- Cognito JWT 서명 검증
- 토큰 만료 시간 확인
- 발급자/대상 검증

### 🚨 트러블슈팅

#### 로그 확인
```bash
# ECS 로그 확인
aws logs tail /ecs/user-service --follow

# 애플리케이션 로그
tail -f logs/user-service.log

# Oracle EC2 연결 테스트
telnet your-oracle-ec2-ip 1521
```

#### 일반적인 문제
1. **데이터베이스 연결 실패**: 
   - Oracle EC2 보안 그룹 및 네트워크 설정 확인
   - Oracle Listener 상태 확인 (`lsnrctl status`)
   - ECS에서 Oracle EC2로의 네트워크 연결 테스트
2. **Cognito 인증 실패**: Client Secret 및 설정 확인
3. **MSA 연동 실패**: 서비스 URL 및 네트워크 설정 확인

### 📞 지원
- **팀**: Team-FOG
- **담당자**: User Service 개발팀
- **문서**: `docs/` 폴더 참조

---

**버전**: 1.0  
**최종 업데이트**: 2024년 1월 15일
