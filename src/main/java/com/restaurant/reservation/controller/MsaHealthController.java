package com.restaurant.reservation.controller;

import com.restaurant.reservation.service.MsaHealthCheckService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * MSA 환경에서 다른 서비스들의 상태를 확인하는 헬스체크 컨트롤러
 * 
 * 이 컨트롤러는 다른 마이크로서비스들이 정상적으로 동작하는지 확인하는 API를 제공합니다.
 * 
 * 주요 기능:
 * 1. 모든 등록된 서비스의 헬스체크 수행
 * 2. 특정 서비스의 헬스체크 수행
 * 3. 서비스별 상태 정보 조회
 * 4. MSA 환경 모니터링
 * 
 * @author Team-FOG
 * @version 1.0
 */
@RestController
@RequestMapping("/msa/health")
@CrossOrigin(origins = "*")
public class MsaHealthController {
    
    private static final Logger logger = LoggerFactory.getLogger(MsaHealthController.class);
    
    private final MsaHealthCheckService msaHealthCheckService;
    
    public MsaHealthController(MsaHealthCheckService msaHealthCheckService) {
        this.msaHealthCheckService = msaHealthCheckService;
    }
    
    /**
     * 모든 등록된 서비스의 헬스체크를 수행합니다.
     * 
     * @return 서비스별 헬스체크 결과
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, MsaHealthCheckService.ServiceHealthStatus>> checkAllServicesHealth() {
        try {
            logger.info("전체 서비스 헬스체크 요청");
            
            Map<String, MsaHealthCheckService.ServiceHealthStatus> healthStatuses = 
                msaHealthCheckService.checkAllServicesHealth();
            
            logger.info("전체 서비스 헬스체크 완료: {}개 서비스", healthStatuses.size());
            return ResponseEntity.ok(healthStatuses);
            
        } catch (Exception e) {
            logger.error("전체 서비스 헬스체크 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 특정 서비스의 헬스체크를 수행합니다.
     * 
     * @param serviceName 서비스 이름
     * @return 해당 서비스의 헬스체크 결과
     */
    @GetMapping("/{serviceName}")
    public ResponseEntity<MsaHealthCheckService.ServiceHealthStatus> checkServiceHealth(
            @PathVariable String serviceName) {
        try {
            logger.info("서비스 헬스체크 요청: {}", serviceName);
            
            MsaHealthCheckService.ServiceHealthStatus healthStatus = 
                msaHealthCheckService.checkServiceHealth(serviceName);
            
            logger.info("서비스 헬스체크 완료: {} - {}", serviceName, healthStatus.isHealthy());
            return ResponseEntity.ok(healthStatus);
            
        } catch (Exception e) {
            logger.error("서비스 헬스체크 중 오류 발생: {}", serviceName, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 특정 서비스의 헬스체크를 비동기로 수행합니다.
     * 
     * @param serviceName 서비스 이름
     * @return 해당 서비스의 헬스체크 결과 (비동기)
     */
    @GetMapping("/async/{serviceName}")
    public ResponseEntity<MsaHealthCheckService.ServiceHealthStatus> checkServiceHealthAsync(
            @PathVariable String serviceName) {
        try {
            logger.info("비동기 서비스 헬스체크 요청: {}", serviceName);
            
            MsaHealthCheckService.ServiceHealthStatus healthStatus = 
                msaHealthCheckService.checkServiceHealthAsync(serviceName).get();
            
            logger.info("비동기 서비스 헬스체크 완료: {} - {}", serviceName, healthStatus.isHealthy());
            return ResponseEntity.ok(healthStatus);
            
        } catch (Exception e) {
            logger.error("비동기 서비스 헬스체크 중 오류 발생: {}", serviceName, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * MSA 환경의 전체 상태 요약을 제공합니다.
     * 
     * @return MSA 환경 상태 요약
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getMsaHealthSummary() {
        try {
            logger.info("MSA 헬스체크 요약 요청");
            
            Map<String, MsaHealthCheckService.ServiceHealthStatus> healthStatuses = 
                msaHealthCheckService.checkAllServicesHealth();
            
            // 요약 정보 생성
            long healthyCount = healthStatuses.values().stream()
                .filter(MsaHealthCheckService.ServiceHealthStatus::isHealthy)
                .count();
            
            long totalCount = healthStatuses.size();
            double healthPercentage = totalCount > 0 ? (double) healthyCount / totalCount * 100 : 0;
            
            Map<String, Object> summary = Map.of(
                "totalServices", totalCount,
                "healthyServices", healthyCount,
                "unhealthyServices", totalCount - healthyCount,
                "healthPercentage", Math.round(healthPercentage * 100.0) / 100.0,
                "overallStatus", healthyCount == totalCount ? "HEALTHY" : "DEGRADED",
                "serviceDetails", healthStatuses
            );
            
            logger.info("MSA 헬스체크 요약 완료: {}/{} 서비스 정상 ({}%)", 
                healthyCount, totalCount, Math.round(healthPercentage * 100.0) / 100.0);
            
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            logger.error("MSA 헬스체크 요약 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
