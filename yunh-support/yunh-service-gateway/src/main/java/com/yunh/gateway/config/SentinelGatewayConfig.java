package com.yunh.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.exception.SentinelGatewayBlockExceptionHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
public class SentinelGatewayConfig {

    private static final int URL_MATCH_STRATEGY_PREFIX = 2;

    private final List<ViewResolver> viewResolvers;
    private final ServerCodecConfigurer serverCodecConfigurer;

    public SentinelGatewayConfig(
            ObjectProvider<List<ViewResolver>> viewResolversProvider,
            ServerCodecConfigurer serverCodecConfigurer) {
        this.viewResolvers = viewResolversProvider.getIfAvailable(Collections::emptyList);
        this.serverCodecConfigurer = serverCodecConfigurer;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler() {
        return new SentinelGatewayBlockExceptionHandler(viewResolvers, serverCodecConfigurer);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public GlobalFilter sentinelGatewayFilter() {
        return new SentinelGatewayFilter();
    }

    @PostConstruct
    public void init() {
        initCustomizedBlockHandler();
        initApiDefinitions();
        initGatewayRules();
    }

    private void initGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();
        rules.add(new GatewayFlowRule("yunh-service-user")
                .setCount(2)
                .setIntervalSec(1));
        rules.add(new GatewayFlowRule("yunh-service-course")
                .setCount(2)
                .setIntervalSec(1));
        GatewayRuleManager.loadRules(rules);
    }

    private void initApiDefinitions() {
        Set<ApiDefinition> definitions = new HashSet<>();
        Set<ApiPredicateItem> userPredicates = new HashSet<>();
        userPredicates.add(new ApiPathPredicateItem()
                .setPattern("/api/user/**")
                .setMatchStrategy(URL_MATCH_STRATEGY_PREFIX));
        definitions.add(new ApiDefinition("user-api").setPredicateItems(userPredicates));

        Set<ApiPredicateItem> coursePredicates = new HashSet<>();
        coursePredicates.add(new ApiPathPredicateItem()
                .setPattern("/api/course/**")
                .setMatchStrategy(URL_MATCH_STRATEGY_PREFIX));
        definitions.add(new ApiDefinition("course-api").setPredicateItems(coursePredicates));

        GatewayApiDefinitionManager.loadApiDefinitions(definitions);
    }

    private void initCustomizedBlockHandler() {
        GatewayCallbackManager.setBlockHandler(new BlockRequestHandler() {
            @Override
            public Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable t) {
                return ServerResponse
                        .status(HttpStatus.TOO_MANY_REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(
                                "{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\"}"
                        ));
            }
        });
    }
}
