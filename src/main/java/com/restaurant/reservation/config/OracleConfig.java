package com.restaurant.reservation.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Oracle DB 특화 설정
 * AWS ECS 환경에서 Oracle DB 연결을 위한 설정
 */
@Configuration
@Profile("prod")
public class OracleConfig {

    /**
     * Oracle DB 연결 풀 모니터링
     */
    @Bean
    @ConditionalOnProperty(name = "spring.profiles.active", havingValue = "prod")
    public DataSourceHealthIndicator oracleDataSourceHealthIndicator(DataSource dataSource) {
        return new DataSourceHealthIndicator(dataSource, "SELECT 1 FROM DUAL");
    }

    /**
     * Oracle DB 특화 설정
     */
    @Bean
    @ConditionalOnProperty(name = "spring.profiles.active", havingValue = "prod")
    public OracleConnectionProperties oracleConnectionProperties() {
        return new OracleConnectionProperties();
    }

    /**
     * Oracle DB 연결 풀 설정
     */
    @Bean
    @ConditionalOnProperty(name = "spring.profiles.active", havingValue = "prod")
    public OraclePoolConfig oraclePoolConfig() {
        return new OraclePoolConfig();
    }

    /**
     * Oracle DB 연결 속성
     */
    public static class OracleConnectionProperties {
        private int timeout = 30;
        private int initialSize = 5;
        private int maxSize = 20;
        private int minIdle = 5;
        private boolean ssl = false;
        private boolean encryption = false;

        // Getters and Setters
        public int getTimeout() { return timeout; }
        public void setTimeout(int timeout) { this.timeout = timeout; }
        public int getInitialSize() { return initialSize; }
        public void setInitialSize(int initialSize) { this.initialSize = initialSize; }
        public int getMaxSize() { return maxSize; }
        public void setMaxSize(int maxSize) { this.maxSize = maxSize; }
        public int getMinIdle() { return minIdle; }
        public void setMinIdle(int minIdle) { this.minIdle = minIdle; }
        public boolean isSsl() { return ssl; }
        public void setSsl(boolean ssl) { this.ssl = ssl; }
        public boolean isEncryption() { return encryption; }
        public void setEncryption(boolean encryption) { this.encryption = encryption; }
    }

    /**
     * Oracle DB 연결 풀 설정
     */
    public static class OraclePoolConfig {
        private int maximumPoolSize = 20;
        private int minimumIdle = 5;
        private int connectionTimeout = 30000;
        private int idleTimeout = 600000;
        private int maxLifetime = 1800000;
        private int leakDetectionThreshold = 60000;

        // Getters and Setters
        public int getMaximumPoolSize() { return maximumPoolSize; }
        public void setMaximumPoolSize(int maximumPoolSize) { this.maximumPoolSize = maximumPoolSize; }
        public int getMinimumIdle() { return minimumIdle; }
        public void setMinimumIdle(int minimumIdle) { this.minimumIdle = minimumIdle; }
        public int getConnectionTimeout() { return connectionTimeout; }
        public void setConnectionTimeout(int connectionTimeout) { this.connectionTimeout = connectionTimeout; }
        public int getIdleTimeout() { return idleTimeout; }
        public void setIdleTimeout(int idleTimeout) { this.idleTimeout = idleTimeout; }
        public int getMaxLifetime() { return maxLifetime; }
        public void setMaxLifetime(int maxLifetime) { this.maxLifetime = maxLifetime; }
        public int getLeakDetectionThreshold() { return leakDetectionThreshold; }
        public void setLeakDetectionThreshold(int leakDetectionThreshold) { this.leakDetectionThreshold = leakDetectionThreshold; }
    }

    /**
     * DataSource 헬스체크 인디케이터
     */
    public static class DataSourceHealthIndicator {
        private final DataSource dataSource;
        private final String validationQuery;

        public DataSourceHealthIndicator(DataSource dataSource, String validationQuery) {
            this.dataSource = dataSource;
            this.validationQuery = validationQuery;
        }

        public boolean isHealthy() {
            try (var connection = dataSource.getConnection();
                 var statement = connection.createStatement();
                 var resultSet = statement.executeQuery(validationQuery)) {
                return resultSet.next();
            } catch (SQLException e) {
                return false;
            }
        }

        public DataSource getDataSource() { return dataSource; }
        public String getValidationQuery() { return validationQuery; }
    }
}
