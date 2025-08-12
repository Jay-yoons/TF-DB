# Runtime image (Java 17)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Container-aware JVM tuning (optional)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Copy built jar (use wildcard for versioned jar)
COPY build/libs/*.jar app.jar

# Expose application port
EXPOSE 8080

# Run
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
