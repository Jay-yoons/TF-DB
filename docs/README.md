# 📚 Team-FOG 프로젝트 문서 목록

## 📋 개요

이 폴더는 Team-FOG 프로젝트의 모든 문서를 포함합니다. 각 문서는 프로젝트의 특정 영역에 대한 상세한 가이드와 설명을 제공합니다.

## 📁 문서 분류

### 🚀 **배포 및 인프라 관련**

#### AWS MSA 설정
- **[AWS_MSA_SETUP_GUIDE.md](./AWS_MSA_SETUP_GUIDE.md)** - AWS MSA 환경 구축 가이드
- **[infrastructure-setup.md](./infrastructure-setup.md)** - AWS 인프라 설정 가이드

#### 배포 가이드
- **[DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md)** - AWS ECS 배포 가이드
- **[PRODUCTION_DEPLOYMENT_GUIDE.md](./PRODUCTION_DEPLOYMENT_GUIDE.md)** - 프로덕션 배포 가이드

### 🔧 **개발 및 기술 관련**

#### API 및 연동
- **[API_DOCUMENTATION.md](./API_DOCUMENTATION.md)** - User Service API 문서
- **[MSA_INTEGRATION.md](./MSA_INTEGRATION.md)** - MSA 연동 가이드

#### 데이터베이스
- **[COMPLETE_DB_SCHEMA.md](./COMPLETE_DB_SCHEMA.md)** - 전체 데이터베이스 스키마

## 📖 문서 사용 가이드

### 🚀 **새로운 팀원을 위한 시작 가이드**

1. **[README.md](../README.md)** - 프로젝트 개요 및 빠른 시작
2. **[AWS_MSA_SETUP_GUIDE.md](./AWS_MSA_SETUP_GUIDE.md)** - AWS 환경 구축
3. **[COMPLETE_DB_SCHEMA.md](./COMPLETE_DB_SCHEMA.md)** - 데이터베이스 구조 이해
4. **[API_DOCUMENTATION.md](./API_DOCUMENTATION.md)** - API 사용법 학습

### 🔧 **개발자를 위한 가이드**

1. **[MSA_INTEGRATION.md](./MSA_INTEGRATION.md)** - 서비스 간 연동 방법
2. **[DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md)** - 개발 환경 배포
3. **[API_DOCUMENTATION.md](./API_DOCUMENTATION.md)** - API 개발 참고

### 🚀 **DevOps 담당자를 위한 가이드**

1. **[infrastructure-setup.md](./infrastructure-setup.md)** - 인프라 설정
2. **[DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md)** - 배포 과정
3. **[PRODUCTION_DEPLOYMENT_GUIDE.md](./PRODUCTION_DEPLOYMENT_GUIDE.md)** - 프로덕션 배포

## 📝 **문서 업데이트 가이드**

### 새로운 문서 추가 시
1. 적절한 카테고리에 분류
2. 이 README.md 파일에 링크 추가
3. 문서명은 `대문자_언더스코어.md` 형식 사용

### 문서 수정 시
1. 변경 사항을 명확히 기록
2. 관련 문서들도 함께 업데이트
3. 버전 정보 업데이트

## 🔗 **관련 링크**

- **프로젝트 루트**: [../README.md](../README.md)
- **AWS 설정**: [../aws/](../aws/)
- **소스 코드**: [../src/](../src/)

## 📊 **현재 팀 상황**

### 🏗️ **프로젝트 구조**
- **User Service**: 사용자 인증, 회원가입, 마이페이지 (포트: 8082)
- **Store Service**: 가게 관리, 리뷰 시스템 (포트: 8081)
- **Reservation Service**: 예약 관리, 대기열 (포트: 8080)

### 🔧 **기술 스택**
- **Backend**: Spring Boot 3.5.4, Java 17
- **Database**: Oracle PDB + Standby
- **Cloud**: AWS ECS Fargate
- **Authentication**: AWS Cognito

### 👥 **팀 구성**
- **User Service 담당자**: 사용자 인증/인가, 마이페이지
- **Store Service 담당자**: 가게 관리, 리뷰 시스템
- **Reservation Service 담당자**: 예약 관리, 대기열
- **DevOps 담당자**: AWS 인프라, 배포 관리

---

📞 **문의사항**: 문서 관련 문의는 팀 리더에게 연락하세요.
