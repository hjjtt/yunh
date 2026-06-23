package com.yunh.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 用户服务配置属性类
 * 
 * 学习要点：
 * 1. @ConfigurationProperties - 绑定配置前缀
 * 2. @RefreshScope - 配置变更时自动刷新
 * 3. @Component - 注册为 Spring Bean
 * 
 * @author yunh
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "yunh.user")
public class UserProperties {
    
    /**
     * 欢迎语（用于测试动态刷新）
     */
    private String welcome;
    
    /**
     * 服务版本号
     */
    private String version;
}
