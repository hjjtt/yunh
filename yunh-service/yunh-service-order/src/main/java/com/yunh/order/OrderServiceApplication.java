package com.yunh.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 订单服务启动类
 * 
 * 端口: 9004
 * 职责: 订单管理、购物车
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.yunh.api.client")
@ComponentScan(basePackages = {"com.yunh.order", "com.yunh.api.client.fallback"})
@MapperScan("com.yunh.order.mapper")
public class OrderServiceApplication {
    
    public static void main(String[] args) {
        System.setProperty("csp.sentinel.log.dir", "D:\\vis\\yunh\\log\\sentinel");
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
