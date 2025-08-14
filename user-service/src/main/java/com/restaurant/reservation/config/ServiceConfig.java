package com.restaurant.reservation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import lombok.Getter;

import java.time.Duration;

@Configuration
@Getter
public class ServiceConfig {

    @Value("${service.booking.url:http://booking-service:8080}")
    private String bookingServiceUrl;

    @Value("${service.store.url:http://store-service:8080}")
    private String storeServiceUrl;

    @Value("${service.review.url:http://review-service:8080}")
    private String reviewServiceUrl;

    @Value("${service.notification.url:http://notification-service:8080}")
    private String notificationServiceUrl;

    @Value("${service.map.url:http://map-service:8080}")
    private String mapServiceUrl;

    @Value("${service.gateway.url:http://gateway-service:8080}")
    private String gatewayServiceUrl;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }
}