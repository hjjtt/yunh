package com.yunh.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "yunh.jwt")
public class JwtProperties {

    private String secret;
    private long accessTokenExpire = 7200000L;
    private long refreshTokenExpire = 604800000L;
    private String issuer = "yunh-auth";
}
