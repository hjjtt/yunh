package com.yunh.course;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 课程服务启动类
 * 
 * 第二阶段：
 * - @EnableDiscoveryClient 启用服务注册与发现
 * - @EnableFeignClients 启用 Feign 客户端
 * 
 * 第五阶段：
 * - @ComponentScan 扩展扫描范围，包含 api 模块的 Fallback 类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.yunh.api.client")
@ComponentScan(basePackages = {"com.yunh.course", "com.yunh.api.client.fallback"})
@MapperScan("com.yunh.course.mapper")
public class CourseServiceApplication {
    
    public static void main(String[] args) {
        System.setProperty("csp.sentinel.log.dir", "D:\\vis\\yunh\\log\\sentinel");
        SpringApplication.run(CourseServiceApplication.class, args);
    }
}
