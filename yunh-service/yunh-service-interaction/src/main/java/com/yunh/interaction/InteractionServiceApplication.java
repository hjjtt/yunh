package com.yunh.interaction;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 互动服务启动类
 * 
 * 端口: 9007
 * 职责: 评论、问答、笔记
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.yunh.interaction.mapper")
public class InteractionServiceApplication {
    
    public static void main(String[] args) {
        System.setProperty("csp.sentinel.log.dir", "D:\\vis\\yunh\\log\\sentinel");
        SpringApplication.run(InteractionServiceApplication.class, args);
    }
}
