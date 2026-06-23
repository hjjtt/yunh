package com.yunh.pay;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 支付服务启动类
 * 
 * 端口: 9005
 * 职责: 支付对接、退款
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.yunh.api.client")
@ComponentScan(basePackages = {"com.yunh.pay", "com.yunh.api.client.fallback"})
@MapperScan("com.yunh.pay.mapper")
public class PayServiceApplication {
    
    public static void main(String[] args) {
        System.setProperty("csp.sentinel.log.dir", "D:\\vis\\yunh\\log\\sentinel");
        SpringApplication.run(PayServiceApplication.class, args);
    }
}
