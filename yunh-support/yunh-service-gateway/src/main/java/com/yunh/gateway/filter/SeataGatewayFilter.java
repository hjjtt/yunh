package com.yunh.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Seata 事务ID传递过滤器
 * 将Gateway接收到的XID通过请求头传递给下游服务
 */
@Slf4j
@Component
public class SeataGatewayFilter implements GlobalFilter, Ordered {

    private static final String TX_XID = "TX_XID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String xid = request.getHeaders().getFirst(TX_XID);
        if (xid != null && !xid.isEmpty()) {
            log.debug("[Seata] 透传 XID: {}", xid);
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header(TX_XID, xid)
                    .build();
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -250;
    }
}
