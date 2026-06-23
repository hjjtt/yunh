package com.yunh.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 用户服务启动类
 *
 * 第二阶段：添加 @EnableDiscoveryClient 启用服务注册与发现
 * 第五阶段：添加 @EnableFeignClients 启用 OpenFeign 声明式服务调用
 *          @ComponentScan 扩展扫描范围，包含 api 模块的 Fallback 类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.yunh.api.client")
@ComponentScan(basePackages = {"com.yunh.user", "com.yunh.api.client.fallback"})
@MapperScan("com.yunh.user.mapper")
public class UserServiceApplication {
    
    public static void main(String[] args) {
        System.setProperty("csp.sentinel.log.dir", "D:\\vis\\yunh\\log\\sentinel");
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
