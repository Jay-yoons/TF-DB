package com.restaurant.reservation.service;

import com.restaurant.reservation.config.MsaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * MSA 환경에서 다른 서비스들의 상태를 확인하는 헬스체크 서비스
 * 
 * 이 서비스는 다른 마이크로서비스들이 정상적으로 동작하는지 확인합니다.
 * 
 * 주요 기능:
 * 1. 각 서비스의 헬스체크 엔드포인트 호출
 * 2. 비동기 헬스체크 수행
 * 3. 서비스별 상태 정보 수집
 * 4. 장애 서비스 감지 및 로깅
 * 
 * @author Team-FOG
 * @version 1.0
 */
@Service
public class MsaHealthCheckService {
    
    private static final Logger logger = LoggerFactory.getLogger(MsaHealthCheckService.class);
    
    private final RestTemplate restTemplate;
    private final MsaConfig msaConfig;
    
    public MsaHealthCheckService(RestTemplate restTemplate, MsaConfig msaConfig) {
        this.restTemplate = restTemplate;
        this.msaConfig = msaConfig;
    }
    
    /**
     * 모든 등록된 서비스의 헬스체크를 수행합니다.
     */
    public Map<String, ServiceHealthStatus> checkAllServicesHealth() {
        Map<String, ServiceHealthStatus> healthStatuses = new HashMap<>();
        
        for (String serviceName : msaConfig.getServiceUrls().keySet()) {
            CompletableFuture<ServiceHealthStatus> future = checkServiceHealthAsync(serviceName);
            future.thenAccept(status -> healthStatuses.put(serviceName, status));
        }
        
        // 모든 헬스체크가 완료될 때까지 대기
        try {
            Thread.sleep(msaConfig.getDefaultTimeout());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("헬스체크 대기 중 인터럽트 발생");
        }
        
        return healthStatuses;
    }
    
    /**
     * 특정 서비스의 헬스체크를 비동기로 수행합니다.
     */
    public CompletableFuture<ServiceHealthStatus> checkServiceHealthAsync(String serviceName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return checkServiceHealth(serviceName);
            } catch (Exception e) {
                logger.error("서비스 헬스체크 중 오류 발생: {}", serviceName, e);
                return new ServiceHealthStatus(serviceName, false, "오류 발생: " + e.getMessage());
            }
        });
    }
    
    /**
     * 특정 서비스의 헬스체크를 수행합니다.
     */
    public ServiceHealthStatus checkServiceHealth(String serviceName) {
        String serviceUrl = msaConfig.getServiceUrl(serviceName);
        if (serviceUrl.isEmpty()) {
            logger.warn("서비스 URL이 설정되지 않음: {}", serviceName);
            return new ServiceHealthStatus(serviceName, false, "서비스 URL이 설정되지 않음");
        }
        
        String healthCheckUrl = serviceUrl + "/actuator/health";
        int timeout = msaConfig.getServiceTimeout(serviceName);
        
        try {
            logger.debug("서비스 헬스체크 시작: {} -> {}", serviceName, healthCheckUrl);
            
            ResponseEntity<Map> response = restTemplate.getForEntity(healthCheckUrl, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> healthData = response.getBody();
                String status = (String) healthData.get("status");
                
                boolean isHealthy = "UP".equals(status);
                String message = isHealthy ? "정상 동작" : "상태: " + status;
                
                logger.info("서비스 헬스체크 완료: {} - {}", serviceName, message);
                return new ServiceHealthStatus(serviceName, isHealthy, message);
            } else {
                logger.warn("서비스 헬스체크 실패: {} - HTTP {}", serviceName, response.getStatusCode());
                return new ServiceHealthStatus(serviceName, false, "HTTP " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("서비스 헬스체크 중 오류 발생: {} - {}", serviceName, e.getMessage());
            return new ServiceHealthStatus(serviceName, false, "연결 실패: " + e.getMessage());
        }
    }
    
    /**
     * 서비스 상태 정보를 담는 클래스
     */
    public static class ServiceHealthStatus {
        private final String serviceName;
        private final boolean healthy;
        private final String message;
        private final long timestamp;
        
        public ServiceHealthStatus(String serviceName, boolean healthy, String message) {
            this.serviceName = serviceName;
            this.healthy = healthy;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
        
        // Getters
        public String getServiceName() { return serviceName; }
        public boolean isHealthy() { return healthy; }
        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
        
        @Override
        public String toString() {
            return String.format("ServiceHealthStatus{serviceName='%s', healthy=%s, message='%s', timestamp=%d}",
                    serviceName, healthy, message, timestamp);
        }
    }
}
