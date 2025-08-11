# =============================================================================
# User Service - Docker Multi-Stage Build
# =============================================================================
# 
# 이 Dockerfile은 User Service를 위한 멀티스테이지 빌드를 정의합니다.
# 
# 빌드 과정:
# 1. Builder Stage: Java 21 JDK를 사용하여 애플리케이션 빌드
# 2. Runtime Stage: Java 21 JRE를 사용하여 최소한의 런타임 환경 구성
# 
# 최적화 사항:
# - 멀티스테이지 빌드로 최종 이미지 크기 최소화
# - 보안을 위한 non-root 사용자 사용
# - 프로덕션용 JVM 옵션 설정
# - 헬스체크 포함
# =============================================================================

# =============================================================================
# Builder Stage (빌드 단계)
# =============================================================================

# Java 21 JDK Slim 이미지 사용 (빌드용)
FROM openjdk:21-jdk-slim as builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 빌드 파일 복사
COPY gradlew .                    # Gradle Wrapper 실행 파일
COPY gradle gradle               # Gradle Wrapper 디렉토리
COPY build.gradle .              # 빌드 설정 파일
COPY settings.gradle .           # 프로젝트 설정 파일

# Gradle Wrapper 실행 권한 부여
RUN chmod +x ./gradlew

# 의존성 다운로드 (캐시 레이어 최적화)
RUN ./gradlew dependencies

# 소스 코드 복사
COPY src src

# 애플리케이션 빌드 (테스트 제외)
RUN ./gradlew build -x test

# =============================================================================
# Runtime Stage (런타임 단계)
# =============================================================================

# Java 21 JRE Slim 이미지 사용 (런타임용, JDK보다 작음)
FROM openjdk:21-jre-slim

# 작업 디렉토리 설정
WORKDIR /app

# 보안을 위한 non-root 사용자 생성
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Builder Stage에서 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 로그 디렉토리 생성 및 권한 설정
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

# non-root 사용자로 전환 (보안 강화)
USER appuser

# 포트 노출 (8080번 포트)
EXPOSE 8080

# 헬스체크 설정 (컨테이너 상태 모니터링)
# - interval: 30초마다 체크
# - timeout: 3초 타임아웃
# - start-period: 시작 후 5초 대기
# - retries: 3번 재시도
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 프로덕션용 JVM 옵션 설정
# - Xms512m: 초기 힙 크기 512MB
# - Xmx1024m: 최대 힙 크기 1GB
# - UseG1GC: G1 가비지 컬렉터 사용
# - UseContainerSupport: 컨테이너 환경 인식
# - MaxRAMPercentage=75.0: 컨테이너 메모리의 75% 사용
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
