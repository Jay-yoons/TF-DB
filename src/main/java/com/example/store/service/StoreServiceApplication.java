package com.example.store.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 애플리케이션 진입점.
 * - REST API(Server) 구동을 담당한다.
 * - 프로필/환경설정은 application.properties 및 외부 환경변수로 제어한다.
 */
@SpringBootApplication
public class StoreServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StoreServiceApplication.class, args);
    }

}
