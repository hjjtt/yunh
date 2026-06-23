package com.yunh.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "yunh.captcha")
public class CaptchaProperties {

    private int expireSeconds = 300;
    private int length = 4;
}
