# 🔍 팀원 JDK 버전 확인 가이드

## 📋 GitHub에서 확인하는 방법

### 1. **각 팀원의 저장소에서 확인**

#### User Service (현재 프로젝트)
```bash
# GitHub 저장소: Team-FOG-User
# 확인 파일: build.gradle
sourceCompatibility = JavaVersion.VERSION_21  # Java 21
```

#### Store Service
```bash
# GitHub 저장소: Team-FOG-Store
# 확인 파일: build.gradle 또는 pom.xml
```

#### Reservation Service  
```bash
# GitHub 저장소: Team-FOG-Reservation
# 확인 파일: build.gradle 또는 pom.xml
```

### 2. **확인해야 할 파일들**

#### Gradle 프로젝트
```gradle
// build.gradle
java {
    sourceCompatibility = JavaVersion.VERSION_XX  // 여기서 확인
    targetCompatibility = JavaVersion.VERSION_XX  // 여기서 확인
}
```

#### Maven 프로젝트
```xml
<!-- pom.xml -->
<properties>
    <java.version>21</java.version>  <!-- 여기서 확인 -->
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
</properties>
```

#### Docker 프로젝트
```dockerfile
# Dockerfile
FROM openjdk:21-jdk-slim  # 여기서 확인
# 또는
FROM eclipse-temurin:21-jdk  # 여기서 확인
```

### 3. **GitHub 검색 방법**

#### 저장소별 검색
```bash
# 각 팀원 저장소에서 다음 키워드로 검색:
# - "sourceCompatibility"
# - "java.version"
# - "openjdk:"
# - "FROM openjdk"
# - "JavaVersion.VERSION"
```

#### 전체 팀 저장소 검색
```bash
# GitHub에서 조직/팀 레벨에서 검색:
# - "JavaVersion.VERSION" org:Team-FOG
# - "sourceCompatibility" org:Team-FOG
# - "openjdk:" org:Team-FOG
```

## 🚀 자동화된 확인 스크립트

### 1. **GitHub API를 사용한 확인**

```bash
#!/bin/bash
# check_team_jdk_versions.sh

TEAM_REPOS=(
    "Team-FOG-User"
    "Team-FOG-Store" 
    "Team-FOG-Reservation"
)

echo "🔍 팀원 JDK 버전 확인 중..."
echo "=================================="

for repo in "${TEAM_REPOS[@]}"; do
    echo "📦 $repo"
    
    # build.gradle에서 Java 버전 확인
    java_version=$(curl -s "https://raw.githubusercontent.com/Team-FOG/$repo/main/build.gradle" | \
                   grep -o "JavaVersion.VERSION_[0-9]*" | head -1)
    
    if [ -n "$java_version" ]; then
        echo "   ✅ Java 버전: $java_version"
    else
        echo "   ❌ Java 버전을 찾을 수 없음"
    fi
    
    # Dockerfile에서 Java 이미지 확인
    docker_image=$(curl -s "https://raw.githubusercontent.com/Team-FOG/$repo/main/Dockerfile" | \
                   grep -o "FROM openjdk:[0-9]*" | head -1)
    
    if [ -n "$docker_image" ]; then
        echo "   🐳 Docker 이미지: $docker_image"
    else
        echo "   ❌ Docker 이미지를 찾을 수 없음"
    fi
    
    echo ""
done
```

### 2. **로컬에서 확인하는 방법**

```bash
# 각 팀원의 로컬 프로젝트에서
cd /path/to/teammate/project

# Gradle 프로젝트
./gradlew --version

# Maven 프로젝트  
mvn --version

# Java 버전 직접 확인
java -version
```

## 📊 팀 JDK 버전 현황 체크리스트

### User Service (담당자: [이름])
- [ ] **build.gradle**: `sourceCompatibility = JavaVersion.VERSION_21`
- [ ] **Dockerfile**: `FROM openjdk:21-jdk-slim`
- [ ] **로컬 Java**: Java 21.x
- [ ] **Gradle**: 8.x
- [ ] **Spring Boot**: 3.4.0

### Store Service (담당자: [이름])
- [ ] **build.gradle/pom.xml**: Java 버전 확인
- [ ] **Dockerfile**: Java 이미지 버전 확인
- [ ] **로컬 Java**: Java 버전 확인
- [ ] **빌드 도구**: Gradle/Maven 버전 확인
- [ ] **Spring Boot**: 버전 확인

### Reservation Service (담당자: [이름])
- [ ] **build.gradle/pom.xml**: Java 버전 확인
- [ ] **Dockerfile**: Java 이미지 버전 확인
- [ ] **로컬 Java**: Java 버전 확인
- [ ] **빌드 도구**: Gradle/Maven 버전 확인
- [ ] **Spring Boot**: 버전 확인

## 🚨 버전 불일치 시 해결 방법

### 1. **Java 버전 통일**
```bash
# 모든 팀원이 Java 21 사용하도록 통일
# build.gradle 수정
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

# Dockerfile 수정
FROM openjdk:21-jdk-slim
```

### 2. **Spring Boot 버전 통일**
```gradle
// 모든 팀원이 동일한 Spring Boot 버전 사용
plugins {
    id 'org.springframework.boot' version '3.4.0'
}
```

### 3. **Gradle 버전 통일**
```properties
# gradle/wrapper/gradle-wrapper.properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.5-bin.zip
```

## 📞 팀원별 확인 요청

각 팀원에게 다음 정보를 요청하세요:

1. **프로젝트 설정 파일**
   - `build.gradle` 또는 `pom.xml` 내용
   - `Dockerfile` 내용

2. **로컬 환경 정보**
   ```bash
   java -version
   ./gradlew --version  # 또는 mvn --version
   ```

3. **빌드 테스트 결과**
   ```bash
   ./gradlew clean build -x test
   # 또는
   mvn clean package -DskipTests
   ```

## 🔗 참조 문서

- [Spring Boot Java 버전 호환성](https://spring.io/projects/spring-boot#learn)
- [Gradle Java 버전 설정](https://docs.gradle.org/current/userguide/java_plugin.html)
- [Maven Java 버전 설정](https://maven.apache.org/plugins/maven-compiler-plugin/)
