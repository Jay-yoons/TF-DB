# 🔄 MSA 연동 가이드

## 📋 개요

Team-FOG 프로젝트의 마이크로서비스 간 연동 방법을 설명하는 가이드입니다. 각 서비스 담당자가 다른 서비스와의 연동을 구현할 때 참고할 수 있습니다.

## 🏗️ 전체 MSA 아키텍처

```
┌─────────────────────────────────────────────────────────────────┐
│                        Frontend (Vue.js)                        │
│                           Port: 3000                            │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    API Gateway (Nginx/ALB)                      │
│                           Port: 80/443                          │
└─────────────────────────────────────────────────────────────────┘
                                │
                ┌───────────────┼───────────────┐
                ▼               ▼               ▼
┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│ User Service│ │Store Service│ │Reservation  │ │ Other       │
│ Port: 8082  │ │ Port: 8081  │ │Service      │ │ Services    │
│             │ │             │ │ Port: 8080  │ │             │
└─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘
       │               │               │               │
       └───────────────┼───────────────┼───────────────┘
                       ▼               ▼
              ┌─────────────┐ ┌─────────────┐
              │ User DB     │ │ Store DB    │
              │ (Oracle PDB)│ │ (Oracle PDB)│
              └─────────────┘ └─────────────┘
```

## 🔧 서비스별 연동 정보

### 1. User Service (포트: 8082)

**담당자**: User Service 담당자

**주요 기능**:
- 사용자 인증 (AWS Cognito)
- 회원가입/로그인
- 마이페이지 (리뷰, 즐겨찾기)
- 사용자 정보 관리

**다른 서비스와의 연동**:

#### Store Service 연동
```java
// Store Service에서 리뷰 데이터 가져오기
@GetMapping("/me/reviews")
public ResponseEntity<List<ReviewDto>> getMyReviews() {
    // Store Service API 호출
    List<ReviewDto> reviews = storeServiceIntegration.getUserReviews(userId);
    return ResponseEntity.ok(reviews);
}

// Store Service에서 가게 정보 가져오기
@GetMapping("/me/reviews/{reviewId}/store-info")
public ResponseEntity<Map<String, Object>> getStoreInfoForReview(@PathVariable Long reviewId) {
    // Store Service API 호출
    Map<String, Object> storeInfo = storeServiceIntegration.getStoreInfoForReview(reviewId);
    return ResponseEntity.ok(storeInfo);
}
```

**Store Service API 호출**:
- `GET http://store-service:8081/api/stores/reviews/user/{userId}` - 사용자별 리뷰 목록
- `GET http://store-service:8081/api/stores/reviews/{reviewId}/store-info` - 리뷰별 가게 정보
- `GET http://store-service:8081/api/stores/{storeId}` - 가게 정보

#### Reservation Service 연동
```java
// Reservation Service에서 예약 정보 가져오기 (향후 구현 예정)
@GetMapping("/me/reservations")
public ResponseEntity<List<ReservationDto>> getMyReservations() {
    // Reservation Service API 호출
    List<ReservationDto> reservations = reservationServiceIntegration.getUserReservations(userId);
    return ResponseEntity.ok(reservations);
}
```

### 2. Store Service (포트: 8081)

**담당자**: Store Service 담당자

**주요 기능**:
- 가게 정보 관리
- 리뷰 시스템
- 메뉴 관리

**User Service와의 연동**:

#### User Service API 호출
```java
// 사용자 정보 조회
@GetMapping("/reviews/user/{userId}")
public ResponseEntity<List<ReviewDto>> getUserReviews(@PathVariable String userId) {
    // User Service에서 사용자 정보 확인
    UserInfoDto userInfo = userServiceIntegration.getUserInfo(userId);
    
    // 해당 사용자의 리뷰 목록 반환
    List<ReviewDto> reviews = reviewService.getReviewsByUserId(userId);
    return ResponseEntity.ok(reviews);
}
```

**User Service API 호출**:
- `GET http://user-service:8082/api/users/{userId}` - 사용자 정보 조회
- `GET http://user-service:8082/api/users/me/favorites/{storeId}/check` - 즐겨찾기 상태 확인

### 3. Reservation Service (포트: 8080)

**담당자**: Reservation Service 담당자

**주요 기능**:
- 예약 시스템
- 예약 관리
- 결제 연동

**User Service와의 연동**:

#### User Service API 호출
```java
// 예약 생성 시 사용자 정보 확인
@PostMapping("/reservations")
public ResponseEntity<ReservationDto> createReservation(@RequestBody CreateReservationRequest request) {
    // User Service에서 사용자 정보 확인
    UserInfoDto userInfo = userServiceIntegration.getUserInfo(request.getUserId());
    
    // 예약 생성
    ReservationDto reservation = reservationService.createReservation(request, userInfo);
    return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
}
```

**User Service API 호출**:
- `GET http://user-service:8082/api/users/{userId}` - 사용자 정보 조회
- `GET http://user-service:8082/api/users/me` - 현재 사용자 정보 (토큰 기반)

## 🔄 서비스 간 통신 방식

### 1. HTTP REST API

모든 서비스 간 통신은 HTTP REST API를 사용합니다.

**기본 형식**:
```java
@RestController
@RequestMapping("/api/integration")
public class ServiceIntegrationController {
    
    private final RestTemplate restTemplate;
    private final MsaConfig msaConfig;
    
    public ServiceIntegrationController(RestTemplate restTemplate, MsaConfig msaConfig) {
        this.restTemplate = restTemplate;
        this.msaConfig = msaConfig;
    }
    
    // 다른 서비스 API 호출 예시
    public ResponseEntity<Object> callExternalService(String serviceName, String endpoint) {
        String serviceUrl = msaConfig.getServiceUrl(serviceName);
        String fullUrl = serviceUrl + endpoint;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        return restTemplate.exchange(fullUrl, HttpMethod.GET, entity, Object.class);
    }
}
```

### 2. 서비스 URL 설정

**application.yml**:
```yaml
msa:
  service-urls:
    user-service: http://user-service:8082
    store-service: http://store-service:8081
    reservation-service: http://reservation-service:8080
    frontend: http://frontend:3000
  
  timeouts:
    user-service: 5000
    store-service: 5000
    reservation-service: 5000
  
  retry-counts:
    user-service: 3
    store-service: 3
    reservation-service: 3
```

### 3. 서킷 브레이커 패턴

서비스 간 통신의 안정성을 위해 서킷 브레이커 패턴을 적용합니다.

```java
@Service
public class ServiceIntegrationService {
    
    private final CircuitBreaker circuitBreaker;
    
    public ServiceIntegrationService(CircuitBreakerFactory circuitBreakerFactory) {
        this.circuitBreaker = circuitBreakerFactory.create("external-service");
    }
    
    public ResponseEntity<Object> callWithCircuitBreaker(String serviceUrl) {
        return circuitBreaker.run(
            () -> restTemplate.getForEntity(serviceUrl, Object.class),
            throwable -> handleFallback(throwable)
        );
    }
    
    private ResponseEntity<Object> handleFallback(Throwable throwable) {
        // 폴백 로직 구현
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of("error", "서비스 일시적 사용 불가"));
    }
}
```

## 🔐 인증 및 보안

### 1. 서비스 간 인증

서비스 간 통신에서는 JWT 토큰을 사용하여 인증을 처리합니다.

```java
@Component
public class ServiceAuthenticationFilter {
    
    public HttpHeaders createAuthenticatedHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Service-Name", "user-service");
        headers.set("X-Request-ID", generateRequestId());
        
        // 서비스 간 인증 토큰 추가 (필요시)
        headers.set("X-Service-Token", generateServiceToken());
        
        return headers;
    }
}
```

### 2. CORS 설정

프론트엔드와의 통신을 위한 CORS 설정:

```java
@Configuration
public class CorsConfig {
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://team-fog-frontend.com"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

## 📊 모니터링 및 로깅

### 1. 서비스 간 통신 로깅

```java
@Slf4j
@Service
public class ServiceIntegrationService {
    
    public ResponseEntity<Object> callExternalService(String serviceName, String endpoint) {
        String serviceUrl = msaConfig.getServiceUrl(serviceName);
        String fullUrl = serviceUrl + endpoint;
        
        log.info("외부 서비스 호출 시작: {} -> {}", serviceName, fullUrl);
        
        try {
            ResponseEntity<Object> response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, Object.class);
            log.info("외부 서비스 호출 성공: {} - HTTP {}", serviceName, response.getStatusCode());
            return response;
        } catch (Exception e) {
            log.error("외부 서비스 호출 실패: {} - {}", serviceName, e.getMessage());
            throw e;
        }
    }
}
```

### 2. 헬스체크

각 서비스는 헬스체크 엔드포인트를 제공합니다:

```java
@RestController
public class HealthController {
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "user-service");
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        response.put("version", "1.0.0");
        
        return ResponseEntity.ok(response);
    }
}
```

## 🧪 테스트

### 1. 서비스 간 통신 테스트

```java
@SpringBootTest
class ServiceIntegrationTest {
    
    @Autowired
    private ServiceIntegrationService integrationService;
    
    @Test
    void testStoreServiceIntegration() {
        // Store Service 호출 테스트
        ResponseEntity<List<ReviewDto>> response = integrationService.getUserReviews("user123");
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }
}
```

### 2. Mock 서버 테스트

```java
@SpringBootTest
class ServiceIntegrationWithMockTest {
    
    @MockBean
    private RestTemplate restTemplate;
    
    @Test
    void testWithMockServer() {
        // Mock 응답 설정
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Object.class)))
            .thenReturn(ResponseEntity.ok(mockResponse));
        
        // 테스트 실행
        ResponseEntity<Object> response = integrationService.callExternalService("store-service", "/api/stores");
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
```

## 🚨 에러 처리

### 1. 서비스 연결 실패 처리

```java
@ControllerAdvice
public class ServiceIntegrationExceptionHandler {
    
    @ExceptionHandler(ServiceConnectionException.class)
    public ResponseEntity<Map<String, Object>> handleServiceConnectionException(ServiceConnectionException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "서비스 연결 실패");
        response.put("message", e.getMessage());
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
    
    @ExceptionHandler(ServiceHttpException.class)
    public ResponseEntity<Map<String, Object>> handleServiceHttpException(ServiceHttpException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "서비스 HTTP 오류");
        response.put("message", e.getMessage());
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }
}
```

### 2. 재시도 로직

```java
@Service
public class RetryableServiceIntegration {
    
    @Retryable(value = {ServiceConnectionException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public ResponseEntity<Object> callWithRetry(String serviceUrl) {
        return restTemplate.getForEntity(serviceUrl, Object.class);
    }
    
    @Recover
    public ResponseEntity<Object> recover(ServiceConnectionException e) {
        // 재시도 실패 시 폴백 로직
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of("error", "서비스 일시적 사용 불가"));
    }
}
```

## 📋 체크리스트

### 서비스 연동 구현 시 확인사항

- [ ] 서비스 URL 설정 (application.yml)
- [ ] RestTemplate Bean 설정
- [ ] 서킷 브레이커 설정
- [ ] 재시도 로직 구현
- [ ] 에러 처리 구현
- [ ] 로깅 설정
- [ ] 헬스체크 엔드포인트 구현
- [ ] CORS 설정
- [ ] 단위 테스트 작성
- [ ] 통합 테스트 작성

### 배포 시 확인사항

- [ ] 서비스 간 네트워크 연결 확인
- [ ] 포트 설정 확인
- [ ] 환경변수 설정 확인
- [ ] 보안 그룹 설정 확인
- [ ] 로드 밸런서 설정 확인
- [ ] 모니터링 설정 확인

## 📞 연락처

- **User Service 담당자**: [담당자명]
- **Store Service 담당자**: [담당자명]
- **Reservation Service 담당자**: [담당자명]
- **Frontend 담당자**: [담당자명]
- **DevOps 담당자**: [담당자명]

## 📚 추가 문서

- [User Service 가이드](../README.md)
- [API 문서](API_DOCUMENTATION.md)
- [AWS MSA 세팅 가이드](AWS_MSA_SETUP_GUIDE.md)
- [배포 가이드](DEPLOYMENT_GUIDE.md)
