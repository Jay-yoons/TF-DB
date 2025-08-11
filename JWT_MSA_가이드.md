# 🚀 JWT 기반 MSA 인증 시스템 가이드

## 📋 목차
1. [개요](#개요)
2. [JWT vs HttpSession 비교](#jwt-vs-httpsession-비교)
3. [MSA 환경에서의 JWT 활용](#msa-환경에서의-jwt-활용)
4. [시스템 아키텍처](#시스템-아키텍처)
5. [구현된 주요 클래스들](#구현된-주요-클래스들)
6. [사용 방법](#사용-방법)
7. [테스트 방법](#테스트-방법)
8. [보안 고려사항](#보안-고려사항)
9. [문제 해결](#문제-해결)
10. [FAQ](#faq)

---

## 📖 개요

이 문서는 Team-FOG 프로젝트에서 HttpSession 기반 인증을 JWT 토큰 기반 인증으로 전환한 내용을 설명합니다. MSA(Microservice Architecture) 환경에서 서비스 간 인증 정보를 공유하고 확장 가능한 인증 시스템을 구축하는 방법을 다룹니다.

### 🎯 주요 목표
- **확장성 향상**: 서버 여러 대 배포 시 세션 공유 문제 해결
- **성능 개선**: 서버 메모리 사용량 감소
- **MSA 대응**: 서비스 간 토큰 공유로 통합 인증
- **모바일 지원**: 앱에서 토큰 기반 인증 활용

---

## ⚖️ JWT vs HttpSession 비교

| 구분 | HttpSession | JWT 토큰 |
|------|-------------|----------|
| **상태 관리** | Stateful (서버에 세션 저장) | Stateless (서버에 상태 저장 안함) |
| **확장성** | 서버 여러 대 시 세션 공유 필요 | 서버 간 독립적 운영 가능 |
| **메모리 사용** | 서버 메모리 사용량 높음 | 서버 메모리 사용량 낮음 |
| **보안성** | 서버에서 세션 무효화 가능 | 토큰 무효화 어려움 (별도 관리 필요) |
| **구현 복잡도** | 간단함 | 복잡함 |
| **모바일 대응** | 어려움 | 용이함 |
| **MSA 적합성** | 낮음 | 높음 |

### 🔄 전환 이유
1. **확장성 문제**: 서버 여러 대 배포 시 세션 공유 복잡성
2. **MSA 환경**: 서비스 간 인증 정보 공유 필요
3. **성능 최적화**: 서버 메모리 사용량 감소
4. **모바일 앱**: 토큰 기반 인증으로 앱 연동 용이

---

## 🏗️ MSA 환경에서의 JWT 활용

### 🔐 토큰 공유 방식
```
[사용자] → [예약 서비스] → JWT 토큰 발급
                ↓
[메뉴 서비스] ← JWT 토큰 검증 ← [주문 서비스]
                ↓
[결제 서비스] ← JWT 토큰 검증 ← [알림 서비스]
```

### 🎫 토큰 구조
```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "userId": "user123",
    "userName": "홍길동",
    "serviceName": "restaurant-reservation-service",
    "tokenType": "ACCESS",
    "iss": "restaurant-reservation-service",
    "sub": "user123",
    "iat": 1640995200,
    "exp": 1641081600
  },
  "signature": "HMACSHA256(...)"
}
```

---

## 🏛️ 시스템 아키텍처

### 📦 주요 컴포넌트

```
┌─────────────────────────────────────────────────────────────┐
│                    JWT 기반 인증 시스템                      │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │ JwtConfig   │  │ JwtService  │  │ JwtAuthFilter│         │
│  │ (설정 관리)  │  │ (토큰 처리)  │  │ (인증 필터)  │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │LoginController│ │MsaIntegration│ │MsaTestController│     │
│  │ (로그인 처리) │ │ (서비스 통신) │ │ (테스트 API)  │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
└─────────────────────────────────────────────────────────────┘
```

### 🔄 인증 플로우

1. **로그인 요청**
   ```
   POST /login
   {
     "userId": "user123",
     "password": "password123"
   }
   ```

2. **JWT 토큰 발급**
   ```json
   {
     "success": true,
     "accessToken": "eyJhbGciOiJIUzI1NiIs...",
     "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
     "expiresIn": 86400000
   }
   ```

3. **API 요청 시 토큰 전달**
   ```
   GET /api/reservations
   Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
   ```

4. **토큰 검증 및 인증**
   - JwtAuthenticationFilter에서 토큰 검증
   - 유효한 토큰이면 Spring Security Context에 인증 정보 설정

---

## 📚 구현된 주요 클래스들

### 1. JwtConfig (설정 관리)
```java
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    private String secret;           // JWT 시크릿 키
    private long expiration;         // 토큰 만료 시간
    private String issuer;           // 토큰 발급자
    private String[] allowedServices; // 허용된 서비스 목록
}
```

**주요 기능:**
- JWT 토큰 관련 설정 관리
- MSA 환경에서 허용된 서비스 목록 관리
- application.yml에서 설정 값 읽기

### 2. JwtService (토큰 처리)
```java
@Service
public class JwtService {
    public String generateToken(String userId, String userName, String serviceName);
    public String generateRefreshToken(String userId, String serviceName);
    public boolean validateToken(String token);
    public String getUserIdFromToken(String token);
    public String getUserNameFromToken(String token);
}
```

**주요 기능:**
- JWT 토큰 생성 (액세스/리프레시)
- 토큰 검증 및 파싱
- 토큰에서 사용자 정보 추출
- MSA 환경에서 서비스 간 토큰 공유 지원

### 3. JwtAuthenticationFilter (인증 필터)
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain);
}
```

**주요 기능:**
- 모든 HTTP 요청에서 JWT 토큰 검증
- 유효한 토큰이면 Spring Security Context에 인증 정보 설정
- 공개 경로는 인증 제외

### 4. LoginController (로그인 처리)
```java
@RestController
@RequestMapping("/login")
public class LoginController {
    @PostMapping
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest);
    
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody Map<String, String> refreshRequest);
}
```

**주요 기능:**
- JWT 기반 로그인 처리
- 토큰 갱신 API
- 토큰 상태 확인 API
- MSA 서비스 간 토큰 검증 API

### 5. MsaIntegrationService (서비스 통신)
```java
@Service
public class MsaIntegrationService {
    public Map<String, Object> callExternalService(String serviceUrl, String endpoint, String userId, String userName);
    public boolean validateExternalServiceToken(String token, String serviceName);
}
```

**주요 기능:**
- 다른 마이크로서비스와의 HTTP 통신
- JWT 토큰을 포함한 서비스 간 API 호출
- 외부 서비스 토큰 검증

---

## 🛠️ 사용 방법

### 1. 의존성 추가 (build.gradle)
```gradle
// JWT 라이브러리
implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.3'

// MSA 환경을 위한 의존성
implementation 'org.springframework.cloud:spring-cloud-starter-openfeign:4.1.0'
implementation 'org.springframework.cloud:spring-cloud-starter-loadbalancer:4.1.0'
implementation 'org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j:4.1.0'
```

### 2. 설정 파일 (application.yml)
```yaml
# JWT 설정
jwt:
  secret: your-secret-key-here
  expiration: 86400000  # 24시간
  refresh-expiration: 43200000  # 12시간
  issuer: restaurant-reservation-service
  allowed-services:
    - restaurant-reservation-service
    - restaurant-menu-service
    - restaurant-order-service
    - restaurant-payment-service
    - restaurant-notification-service
```

### 3. 컨트롤러에서 사용
```java
@RestController
public class MyController {
    
    @Autowired
    private JwtService jwtService;
    
    @PostMapping("/my-api")
    public ResponseEntity<?> myApi(@RequestHeader("Authorization") String authHeader) {
        // 토큰에서 Bearer 제거
        String token = authHeader.substring(7);
        
        // 토큰 검증
        if (jwtService.validateToken(token)) {
            String userId = jwtService.getUserIdFromToken(token);
            // 비즈니스 로직 처리
            return ResponseEntity.ok("인증 성공: " + userId);
        } else {
            return ResponseEntity.status(401).body("인증 실패");
        }
    }
}
```

### 4. 다른 서비스와 통신
```java
@Service
public class MyService {
    
    @Autowired
    private MsaIntegrationService msaIntegrationService;
    
    public void callOtherService() {
        // 다른 서비스 호출
        Map<String, Object> result = msaIntegrationService.callExternalService(
            "http://localhost:8081",  // 메뉴 서비스 URL
            "/api/menus",            // 엔드포인트
            "user123",               // 사용자 ID
            "홍길동"                  // 사용자 이름
        );
        
        // 결과 처리
        if ((Boolean) result.get("success")) {
            // 성공 처리
        }
    }
}
```

---

## 🧪 테스트 방법

### 1. 토큰 생성 테스트
```bash
curl -X POST http://localhost:8080/api/msa-test/generate-token \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "testuser",
    "userName": "테스트 사용자"
  }'
```

**응답 예시:**
```json
{
  "success": true,
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "message": "테스트 토큰 생성 성공"
}
```

### 2. 토큰 검증 테스트
```bash
curl -X POST http://localhost:8080/api/msa-test/validate-token \
  -H "Content-Type: application/json" \
  -d '{
    "token": "eyJhbGciOiJIUzI1NiIs..."
  }'
```

**응답 예시:**
```json
{
  "valid": true,
  "userId": "testuser",
  "userName": "테스트 사용자",
  "serviceName": "restaurant-reservation-service",
  "remainingTime": 86350000,
  "message": "토큰 검증 성공"
}
```

### 3. 로그인 테스트
```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "password": "password123"
  }'
```

### 4. 보호된 API 테스트
```bash
curl -X GET http://localhost:8080/api/protected-resource \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

### 5. 서비스 간 통신 테스트
```bash
curl -X POST http://localhost:8080/api/msa-test/test-service-communication \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "testuser",
    "userName": "테스트 사용자",
    "serviceUrl": "http://localhost:8081",
    "endpoint": "/api/menus"
  }'
```

---

## 🔒 보안 고려사항

### 1. 시크릿 키 관리
```yaml
# ❌ 잘못된 방법 (소스코드에 하드코딩)
jwt:
  secret: my-secret-key

# ✅ 올바른 방법 (환경변수 사용)
jwt:
  secret: ${JWT_SECRET:default-secret-key}
```

### 2. 토큰 만료 시간 설정
```yaml
jwt:
  expiration: 900000      # 15분 (액세스 토큰)
  refresh-expiration: 604800000  # 7일 (리프레시 토큰)
```

### 3. HTTPS 사용
```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
```

### 4. 토큰 블랙리스트 관리
```java
@Service
public class TokenBlacklistService {
    private Set<String> blacklistedTokens = new ConcurrentHashSet<>();
    
    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
    }
    
    public boolean isBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
}
```

### 5. CORS 설정
```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("https://yourdomain.com"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

---

## 🔧 문제 해결

### 1. 토큰 검증 실패
**문제:** `JWT 토큰 검증 실패` 오류 발생

**해결 방법:**
```java
// 1. 시크릿 키 확인
@Value("${jwt.secret}")
private String secret;

// 2. 토큰 형식 확인
if (!token.startsWith("Bearer ")) {
    throw new IllegalArgumentException("Invalid token format");
}

// 3. 토큰 만료 확인
if (jwtService.isTokenExpired(token)) {
    throw new IllegalArgumentException("Token expired");
}
```

### 2. 서비스 간 통신 실패
**문제:** 다른 서비스 호출 시 401 Unauthorized 오류

**해결 방법:**
```java
// 1. 토큰이 올바르게 전달되는지 확인
HttpHeaders headers = new HttpHeaders();
headers.set("Authorization", "Bearer " + token);

// 2. 서비스 URL 확인
String serviceUrl = "http://localhost:8081"; // 올바른 URL인지 확인

// 3. 허용된 서비스 목록 확인
if (!jwtConfig.isAllowedService(serviceName)) {
    throw new IllegalArgumentException("Service not allowed: " + serviceName);
}
```

### 3. 토큰 만료 처리
**문제:** 토큰 만료 시 자동 갱신 필요

**해결 방법:**
```java
// 클라이언트에서 토큰 만료 감지
if (response.statusCode() == 401) {
    // 리프레시 토큰으로 새로운 액세스 토큰 발급
    String newToken = refreshAccessToken(refreshToken);
    // 원래 요청 재시도
    retryOriginalRequest(newToken);
}
```

### 4. 메모리 누수 방지
**문제:** 토큰 블랙리스트로 인한 메모리 누수

**해결 방법:**
```java
@Service
public class TokenBlacklistService {
    private final Cache<String, Boolean> blacklistedTokens = 
        CacheBuilder.newBuilder()
            .expireAfterWrite(24, TimeUnit.HOURS)  // 24시간 후 자동 삭제
            .maximumSize(10000)                    // 최대 10000개 토큰 저장
            .build();
}
```

---

## ❓ FAQ

### Q1: JWT 토큰을 어디에 저장해야 하나요?
**A:** 
- **웹 브라우저**: localStorage 또는 sessionStorage
- **모바일 앱**: 앱 내부 저장소 (SharedPreferences, Keychain 등)
- **서버**: 토큰을 저장하지 않음 (Stateless)

### Q2: 토큰이 만료되면 어떻게 하나요?
**A:** 
- 리프레시 토큰을 사용하여 새로운 액세스 토큰 발급
- 리프레시 토큰도 만료되면 재로그인 필요

### Q3: 다른 서비스에서도 같은 JWT를 사용할 수 있나요?
**A:** 
- 네, 동일한 시크릿 키를 사용하면 모든 서비스에서 토큰 검증 가능
- 허용된 서비스 목록에 등록된 서비스만 토큰 공유 가능

### Q4: 토큰을 무효화하려면 어떻게 하나요?
**A:** 
- 토큰 블랙리스트에 추가
- 리프레시 토큰을 무효화
- 서버에서 토큰 만료 시간을 단축

### Q5: 보안을 더 강화하려면 어떻게 하나요?
**A:** 
- 토큰 만료 시간을 짧게 설정 (15분 이하)
- HTTPS 사용 필수
- 토큰에 민감한 정보 포함 금지
- 정기적인 시크릿 키 교체

---

## 📞 지원 및 문의

### 🐛 버그 리포트
- GitHub Issues에 버그 내용과 재현 방법을 상세히 작성
- 로그 파일 첨부
- 환경 정보 (OS, Java 버전, Spring Boot 버전) 포함

### 💡 기능 요청
- 새로운 기능이나 개선 사항 제안
- 사용 사례와 기대 효과 설명

### 📚 추가 문서
- [JWT 공식 문서](https://jwt.io/)
- [Spring Security JWT 가이드](https://spring.io/guides/tutorials/spring-security-and-angular-js/)
- [MSA 패턴 가이드](https://microservices.io/)

---

## 📝 변경 이력

| 버전 | 날짜 | 변경 내용 | 작성자 |
|------|------|-----------|--------|
| 1.0 | 2024-01-XX | 초기 버전 - JWT 기반 MSA 인증 시스템 구현 | Team-FOG |

---

**🎯 이 가이드를 통해 팀원들이 JWT 기반 MSA 인증 시스템을 쉽게 이해하고 적용할 수 있기를 바랍니다!**
