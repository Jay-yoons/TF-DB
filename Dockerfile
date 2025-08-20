# 백엔드 런타임 전용 Dockerfile (JDK 17 slim)
# 사전 조건: 프로젝트 루트에 빌드된 JAR 파일이 존재해야 합니다.
# 사용 예:
#   docker build --build-arg JAR_FILE=build/libs/app.jar -t my-backend .
#   docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=prod my-backend


FROM openjdk:17-jdk-slim

# 컨테이너 내부 작업 디렉토리
WORKDIR /app

# 빌드시 전달할 JAR 파일 경로(기본: app.jar)
ARG JAR_FILE=build/libs/*.jar

# 빌드 산출물 JAR 복사
COPY ${JAR_FILE} /app/app.jar

# 기본 JVM/앱 환경 변수
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -Dfile.encoding=UTF-8"
ENV SPRING_PROFILES_ACTIVE=prod

# 애플리케이션 노출 포트 (필요 시 수정)
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]