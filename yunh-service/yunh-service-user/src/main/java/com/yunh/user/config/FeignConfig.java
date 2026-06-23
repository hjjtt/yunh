package com.yunh.user.config;

import com.yunh.common.constant.InternalCallConstant;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.seata.core.context.RootContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Feign 请求拦截器配置
 *
 * 学习要点：
 * 1. RequestInterceptor - Feign 发送请求前的拦截器
 * 2. 传递上游请求的 Header（如 Authorization）到下游服务
 * 3. 传递 Seata XID（全局事务 ID），实现分布式事务上下文传播
 *
 * 注意：此类不加 @Configuration（避免全局生效）
 * 通过 @FeignClient 的 configuration 属性指定
 */
@Slf4j
@Configuration
public class FeignConfig implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        // 传递 Seata 全局事务 ID（XID）
        String xid = RootContext.getXID();
        if (xid != null && !xid.isEmpty()) {
            template.header(RootContext.KEY_XID, xid);
            log.debug("[Feign 拦截器] 传递 Seata XID: {}", xid);
        }

        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();

            // 传递 Authorization Token
            String token = request.getHeader("Authorization");
            if (token != null && !token.isEmpty()) {
                template.header("Authorization", token);
                log.debug("[Feign 拦截器] 传递 Authorization: {}...", token.substring(0, Math.min(20, token.length())));
            }

            // 传递请求 ID（用于链路追踪）
            String requestId = request.getHeader("X-Request-ID");
            if (requestId != null && !requestId.isEmpty()) {
                template.header("X-Request-ID", requestId);
            }
        }

        String path = template.path();
        if (path != null && path.startsWith("/course/stock/")) {
            template.header(InternalCallConstant.HEADER_NAME, InternalCallConstant.HEADER_VALUE);
            log.debug("[Feign 拦截器] 标记课程库存内部调用: {}", path);
        }
    }
}
