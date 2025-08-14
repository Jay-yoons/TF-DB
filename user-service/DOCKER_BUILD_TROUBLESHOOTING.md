# 🐳 Docker 빌드 문제 해결 가이드

## 📋 현재 상태 확인

✅ **빌드 성공 확인됨**
- Gradle 빌드: `BUILD SUCCESSFUL in 16s`
- JAR 파일 생성: `build/libs/` 디렉토리에 생성됨
- 의존성 해결: 모든 의존성이 정상적으로 다운로드됨

## 🚨 일반적인 Docker 빌드 문제들

### 1. **Gradle Wrapper 권한 문제**

**문제**: Linux/Mac 환경에서 `gradlew` 실행 권한이 없음
```bash
# 오류 메시지
/bin/sh: 1: ./gradlew: Permission denied
```

**해결책**:
```bash
# Dockerfile에서 gradlew 권한 설정
RUN chmod +x gradlew
```

### 2. **메모리 부족 문제**

**문제**: 빌드 중 메모리 부족으로 실패
```bash
# 오류 메시지
java.lang.OutOfMemoryError: Java heap space
```

**해결책**:
```dockerfile
# Dockerfile에 JVM 메모리 설정 추가
ENV GRADLE_OPTS="-Xmx2048m -XX:MaxPermSize=512m"
```

### 3. **네트워크 타임아웃 문제**

**문제**: 의존성 다운로드 중 네트워크 타임아웃
```bash
# 오류 메시지
Could not resolve dependencies
```

**해결책**:
```dockerfile
# Dockerfile에 네트워크 타임아웃 설정 추가
ENV GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.internal.http.connectionTimeout=180000 -Dorg.gradle.internal.http.socketTimeout=180000"
```

### 4. **Java 버전 호환성 문제**

**문제**: Java 버전 불일치
```bash
# 오류 메시지
Unsupported major.minor version
```

**해결책**: Dockerfile에서 Java 21 사용 확인
```dockerfile
FROM openjdk:21-jdk-slim AS builder
```

## 🔧 개선된 Dockerfile

```dockerfile
# Multi-stage build for optimized Docker image
FROM openjdk:21-jdk-slim AS builder

# Set environment variables for Gradle
ENV GRADLE_OPTS="-Dorg.gradle.daemon=false -Xmx2048m -XX:MaxPermSize=512m"
ENV GRADLE_HOME="/opt/gradle"
ENV PATH="${GRADLE_HOME}/bin:${PATH}"

# Set working directory
WORKDIR /app

# Install curl for health check
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy gradle files first (for better caching)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies (with retry mechanism)
RUN ./gradlew dependencies --no-daemon --stacktrace || \
    (sleep 10 && ./gradlew dependencies --no-daemon --stacktrace) || \
    (sleep 30 && ./gradlew dependencies --no-daemon --stacktrace)

# Copy source code
COPY src src

# Build the application (with retry mechanism)
RUN ./gradlew build -x test --no-daemon --stacktrace || \
    (sleep 10 && ./gradlew build -x test --no-daemon --stacktrace) || \
    (sleep 30 && ./gradlew build -x test --no-daemon --stacktrace)

# Runtime stage
FROM openjdk:21-jre-slim

# Install curl for health check
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser
USER appuser

# Expose port
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8082/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 🚀 빌드 명령어

### 1. 로컬 테스트 빌드
```bash
# Gradle 빌드 테스트
./gradlew clean build -x test

# Docker 이미지 빌드
docker build -t team-fog-user-service:latest .

# Docker 이미지 실행 테스트
docker run -p 8082:8082 team-fog-user-service:latest
```

### 2. ECR 푸시용 빌드
```bash
# AWS ECR 로그인
aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin [AWS_ACCOUNT_ID].dkr.ecr.ap-northeast-2.amazonaws.com

# 이미지 태그
docker tag team-fog-user-service:latest [AWS_ACCOUNT_ID].dkr.ecr.ap-northeast-2.amazonaws.com/team-fog-user-service:latest

# ECR 푸시
docker push [AWS_ACCOUNT_ID].dkr.ecr.ap-northeast-2.amazonaws.com/team-fog-user-service:latest
```

## 🔍 문제 진단 체크리스트

### 빌드 전 확인사항
- [ ] Java 21 설치 확인
- [ ] Docker 설치 확인
- [ ] AWS CLI 설정 확인
- [ ] 충분한 디스크 공간 확인 (최소 2GB)
- [ ] 네트워크 연결 확인

### 빌드 중 확인사항
- [ ] Gradle 의존성 다운로드 성공
- [ ] Java 컴파일 성공
- [ ] JAR 파일 생성 확인
- [ ] Docker 이미지 빌드 성공
- [ ] 컨테이너 실행 테스트 성공

### 배포 전 확인사항
- [ ] ECR 리포지토리 존재 확인
- [ ] ECR 로그인 성공
- [ ] 이미지 푸시 성공
- [ ] ECS Task Definition 업데이트
- [ ] ECS 서비스 업데이트

## 🚨 특정 오류 해결

### 1. **Gradle Daemon 문제**
```bash
# Gradle Daemon 중지
./gradlew --stop

# Daemon 없이 빌드
./gradlew build --no-daemon
```

### 2. **의존성 캐시 문제**
```bash
# Gradle 캐시 삭제
rm -rf ~/.gradle/caches/

# 의존성 새로 다운로드
./gradlew build --refresh-dependencies
```

### 3. **Docker 빌드 컨텍스트 문제**
```bash
# .dockerignore 파일 확인
cat .dockerignore

# 불필요한 파일 제외
echo "build/" >> .dockerignore
echo ".gradle/" >> .dockerignore
echo ".git/" >> .dockerignore
```

### 4. **포트 충돌 문제**
```bash
# 사용 중인 포트 확인
netstat -tulpn | grep 8082

# 다른 포트로 테스트
docker run -p 8083:8082 team-fog-user-service:latest
```

## 📞 지원 정보

문제가 지속되면 다음 정보를 수집하여 공유하세요:

1. **오류 메시지 전체**
2. **Docker 버전**: `docker --version`
3. **Java 버전**: `java -version`
4. **Gradle 버전**: `./gradlew --version`
5. **시스템 정보**: OS, 메모리, 디스크 공간
6. **네트워크 환경**: 프록시, 방화벽 설정

## 🔗 참조 문서

- [Docker 공식 문서](https://docs.docker.com/)
- [Gradle 공식 문서](https://gradle.org/docs/)
- [Spring Boot Docker 가이드](https://spring.io/guides/gs/spring-boot-docker/)
- [AWS ECR 사용법](https://docs.aws.amazon.com/ecr/latest/userguide/)
