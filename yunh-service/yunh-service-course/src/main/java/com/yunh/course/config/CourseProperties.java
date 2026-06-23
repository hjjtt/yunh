package com.yunh.course.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 课程服务配置属性类
 * 
 * @RefreshScope - 配置变更时自动刷新
 * 
 * @author yunh
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "yunh.course")
public class CourseProperties {
    
    /**
     * 欢迎语
     */
    private String welcome;
    
    /**
     * 服务版本号
     */
    private String version;
}
