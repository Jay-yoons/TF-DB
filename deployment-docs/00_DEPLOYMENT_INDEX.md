# 🚀 Team-FOG AWS ECS MSA 배포 문서 인덱스

## 📋 **문서 목록**

### **📖 가이드 문서**
- **[01_AWS_MSA_DEPLOYMENT_GUIDE.md](01_AWS_MSA_DEPLOYMENT_GUIDE.md)** - 상세한 AWS ECS MSA 배포 가이드
- **[02_INFRA_QUICK_START.md](02_INFRA_QUICK_START.md)** - 인프라 담당자용 빠른 시작 가이드
- **[03_DB_INFO_REQUEST.md](03_DB_INFO_REQUEST.md)** - DB 담당자에게 전달할 Oracle DB 정보 요청서

### **🔧 스크립트 파일**
- **[04_AWS_INFRA_SETUP.sh](04_AWS_INFRA_SETUP.sh)** - AWS 인프라 생성 스크립트
- **[05_DEPLOY_USER_SERVICE.sh](05_DEPLOY_USER_SERVICE.sh)** - User Service 배포 스크립트

### **⚙️ 설정 파일**
- **[06_ECS_TASK_DEFINITION.json](06_ECS_TASK_DEFINITION.json)** - ECS Task Definition 설정
- **[07_DOCKERFILE](07_DOCKERFILE)** - Docker 이미지 빌드 설정
- **[08_APPLICATION_PROD.yml](08_APPLICATION_PROD.yml)** - 프로덕션 환경 애플리케이션 설정
- **[09_BUILD_GRADLE](09_BUILD_GRADLE)** - Gradle 빌드 설정
- **[10_APPLICATION_TEST.yml](10_APPLICATION_TEST.yml)** - 테스트 환경 애플리케이션 설정

## 🚀 **빠른 시작**

### **인프라 담당자 작업 순서**
1. **[02_INFRA_QUICK_START.md](02_INFRA_QUICK_START.md)** 읽기
2. **[04_AWS_INFRA_SETUP.sh](04_AWS_INFRA_SETUP.sh)** 실행
3. **[03_DB_INFO_REQUEST.md](03_DB_INFO_REQUEST.md)** DB 담당자에게 전달
4. **[05_DEPLOY_USER_SERVICE.sh](05_DEPLOY_USER_SERVICE.sh)** 실행

### **개발자 테스트용 실행**
```bash
# 테스트 환경으로 실행 (H2 인메모리 DB 사용)
./gradlew bootRun --args='--spring.profiles.active=test'

# 또는 환경변수로 설정
set SPRING_PROFILES_ACTIVE=test
./gradlew bootRun
```

### **DB 담당자 작업**
1. **[03_DB_INFO_REQUEST.md](03_DB_INFO_REQUEST.md)** 확인
2. Oracle DB 연결 정보 제공
3. 보안 그룹 설정

## 📚 **상세 가이드**
모든 상세 내용은 **[01_AWS_MSA_DEPLOYMENT_GUIDE.md](01_AWS_MSA_DEPLOYMENT_GUIDE.md)**를 참조하세요.

## 🔧 **파일 설명**

| 순번 | 파일명 | 설명 |
|------|--------|------|
| 01 | AWS_MSA_DEPLOYMENT_GUIDE.md | 📖 상세한 배포 가이드 |
| 02 | INFRA_QUICK_START.md | 🚀 인프라 담당자용 빠른 시작 |
| 03 | DB_INFO_REQUEST.md | 📋 DB 담당자용 요청서 |
| 04 | AWS_INFRA_SETUP.sh | 🏗️ AWS 인프라 생성 스크립트 |
| 05 | DEPLOY_USER_SERVICE.sh | 🐳 User Service 배포 스크립트 |
| 06 | ECS_TASK_DEFINITION.json | ⚙️ ECS Task Definition |
| 07 | DOCKERFILE | 🐳 Docker 이미지 설정 |
| 08 | APPLICATION_PROD.yml | ⚙️ 프로덕션 환경 설정 |
| 09 | BUILD_GRADLE | 🔧 Gradle 빌드 설정 |
| 10 | APPLICATION_TEST.yml | 🧪 테스트 환경 설정 |

## 📞 **지원**
문제 발생 시 개발팀에 문의하세요.

---

**🎉 Team-FOG MSA 배포 준비 완료!**
