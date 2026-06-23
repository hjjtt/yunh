package com.yunh.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 搜索服务启动类
 * 
 * 端口: 9008
 * 职责: 课程搜索、推荐
 */
@SpringBootApplication
@EnableDiscoveryClient
public class SearchServiceApplication {
    
    public static void main(String[] args) {
        System.setProperty("csp.sentinel.log.dir", "D:\\vis\\yunh\\log\\sentinel");
        SpringApplication.run(SearchServiceApplication.class, args);
    }
}
