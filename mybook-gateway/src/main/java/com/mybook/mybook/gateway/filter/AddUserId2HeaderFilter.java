package com.mybook.mybook.gateway.filter;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class AddUserId2HeaderFilter implements GlobalFilter {

    private static final String HEADER_USER_ID = "userId";
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Long userId = null;
        try {
            userId = StpUtil.getLoginIdAsLong();
            log.info("## 当前登录的用户 ID: {}", userId);
        } catch (Exception e) {
            // 若没有登录，则直接放行
            chain.filter(exchange);
        }
        Long finalUserId = userId;
        ServerWebExchange newExchange = exchange.mutate()
                .request(builder -> builder.header(HEADER_USER_ID, String.valueOf(finalUserId)))
                .build();
        return chain.filter(newExchange);
    }
}
