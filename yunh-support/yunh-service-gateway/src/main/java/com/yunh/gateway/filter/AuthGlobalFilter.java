package com.yunh.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/auth/**",
            "/api/user/login",
            "/api/user/register",
            "/api/user/health",
            "/api/search/**",
            "/api/statistics/**",
            "/actuator/**",
            "/favicon.ico");

    private static final List<String> INTERNAL_ONLY_LIST = Arrays.asList(
            "/api/user/internal/**",
            "/api/pay/callback",
            "/api/course/stock/**");

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final String TOKEN_BLACKLIST_PREFIX = "token_blacklist:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${yunh.jwt.secret:your_jwt_secret_here}")
    private String jwtSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        HttpMethod method = request.getMethod();

        if (isInternalOnlyPath(path)) {
            log.warn("[拦截] 外部访问内部接口: {}", path);
            return forbidden(exchange, "禁止访问内部接口");
        }

        if (isWhiteList(path, method)) {
            log.debug("[放行] 白名单路径：{}", path);
            return chain.filter(exchange);
        }

        String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (token == null || token.isEmpty()) {
            log.warn("[拦截] 缺少 Token: {}", path);
            return unauthorized(exchange, "未授权：缺少 Token");
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody();

            if (claims.getExpiration().before(new Date())) {
                log.warn("[拦截] Token 已过期: {}", path);
                return unauthorized(exchange, "Token 已过期");
            }

            if ("refresh".equals(claims.get("type", String.class))) {
                log.warn("[拦截] RefreshToken 不能用于接口访问: {}", path);
                return unauthorized(exchange, "请使用 AccessToken 访问");
            }

            // 检查 Token 黑名单（用户已登出）
            try {
                Boolean blacklisted = redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + token);
                if (Boolean.TRUE.equals(blacklisted)) {
                    log.warn("[拦截] Token 已被加入黑名单: {}, userId={}", path, claims.get("userId"));
                    return unauthorized(exchange, "Token 已失效，请重新登录");
                }
            } catch (Exception e) {
                log.error("[警告] Redis 黑名单检查异常，放行请求: {}", e.getMessage());
            }

            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", String.valueOf(claims.get("userId")))
                    .header("X-Username", claims.get("username", String.class))
                    .header("X-Role", claims.get("role", String.class))
                    .build();

            log.debug("[通过] Token 验证成功：{}, userId={}, role={}", path, claims.get("userId"), claims.get("role"));
            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            log.error("[拦截] Token 无效：{} - {}", path, e.getMessage());
            return unauthorized(exchange, "Token 无效或已过期");
        }
    }

    private boolean isWhiteList(String path, HttpMethod method) {
        if (isPublicReadOnlyCoursePath(path, method)) {
            return true;
        }

        return WHITE_LIST.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private boolean isInternalOnlyPath(String path) {
        return INTERNAL_ONLY_LIST.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private boolean isPublicReadOnlyCoursePath(String path, HttpMethod method) {
        if (method != HttpMethod.GET) {
            return false;
        }

        if ("/api/course/list".equals(path) || "/api/course/health".equals(path)) {
            return true;
        }

        if (!path.startsWith("/api/course/")) {
            return false;
        }

        String suffix = path.substring("/api/course/".length());
        if (suffix.isEmpty()) {
            return false;
        }

        String[] segments = suffix.split("/");
        if (segments.length == 1) {
            return isPositiveNumber(segments[0]);
        }

        return segments.length == 2
                && isPositiveNumber(segments[0])
                && "detail".equals(segments[1]);
    }

    private boolean isPositiveNumber(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i += 1) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        String body = String.format("{\"code\":401,\"message\":\"%s\"}", message);
        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        String body = String.format("{\"code\":403,\"message\":\"%s\"}", message);
        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
