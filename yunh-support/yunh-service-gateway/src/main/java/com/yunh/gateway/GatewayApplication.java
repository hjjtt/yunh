package com.yunh.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {
    public static void main(String[] args) {
        System.setProperty("csp.sentinel.app.type", "1");
        System.setProperty("csp.sentinel.log.dir", "D:\\vis\\yunh\\log\\sentinel");
        SpringApplication.run(GatewayApplication.class, args);
    }
}
