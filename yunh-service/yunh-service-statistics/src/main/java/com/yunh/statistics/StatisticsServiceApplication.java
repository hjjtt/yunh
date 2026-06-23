package com.yunh.statistics;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 统计服务启动类
 * 
 * 端口: 9009
 * 职责: 数据统计、报表
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.yunh.statistics.mapper")
public class StatisticsServiceApplication {
    
    public static void main(String[] args) {
        System.setProperty("csp.sentinel.log.dir", "D:\\vis\\yunh\\log\\sentinel");
        SpringApplication.run(StatisticsServiceApplication.class, args);
    }
}
