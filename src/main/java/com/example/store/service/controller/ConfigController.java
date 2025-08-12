package com.example.store.service.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 환경/설정 관련 API 제공 컨트롤러.
 * - 개발 환경에서 Google Maps API Key를 프론트 개발 편의로 노출하기 위한 용도
 * - 운영 환경에서는 보안상 비활성화 또는 제거 권장
 */
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    /** application.properties 에 정의된 Google Maps API Key. */
    @Value("${google.maps.api.key:}")
    private String googleMapsApiKey;

    /**
     * Google Maps API Key 반환.
     * 주의: 운영환경에서의 키 노출은 지양하며, 도메인/아이피 제한 등 보안설정 필수.
     */
    @GetMapping("/maps-key")
    public Map<String, String> getGoogleMapsApiKey() {
        return Map.of("googleMapsApiKey", googleMapsApiKey);
    }
}
