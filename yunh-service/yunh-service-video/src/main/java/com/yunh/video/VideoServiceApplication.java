package com.yunh.video;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
/**
 * 视频服务启动类
 *
 * 端口: 9006
 * 职责: 视频上传、转码、播放
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.yunh.video.mapper")
public class VideoServiceApplication {
    
    public static void main(String[] args) {
        System.setProperty("csp.sentinel.log.dir", "D:\\vis\\yunh\\log\\sentinel");
        SpringApplication.run(VideoServiceApplication.class, args);
    }
}
