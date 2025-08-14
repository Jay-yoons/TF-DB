package com.restaurant.reservation.config;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Oracle Database 설정 클래스
 * * AWS MSA 환경에서 Oracle DB 연결을 위한 설정을 관리합니다.
 * 실제 배포환경 설정
 * * @author Team-FOG
 * @version 1.0
 * @since 2025-08-12
 */
// @Configuration
// @ConfigurationProperties(prefix = "oracle")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"password", "sslTrustStorePassword"}) // 민감 정보는 toString에서 제외
public class OracleConfig {

    private String host;
    private int port = 1521;
    private String serviceName;
    private String username;
    private String password;
    private String url;
    private int maxPoolSize = 20;
    private int minPoolSize = 5;
    private int connectionTimeout = 30000;
    private int idleTimeout = 600000;
    private boolean sslEnabled = false;
    private String sslTrustStore;
    private String sslTrustStorePassword;

    // JDBC URL을 자동으로 생성하는 커스텀 Getter만 유지
    public String getUrl() {
        if (this.url != null && !this.url.isEmpty()) {
            return this.url;
        }
        return String.format("jdbc:oracle:thin:@%s:%d/%s", this.host, this.port, this.serviceName);
    }
}