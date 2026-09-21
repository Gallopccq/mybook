package com.mybook.mybook.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * 跨域配置：网关是前端跨源访问的唯一入口，这里统一放开。
 *
 * 注意 @Order(Ordered.HIGHEST_PRECEDENCE)：Sa-Token 的 SaReactorFilter 同样是一个 WebFilter，
 * 且会拦截 OPTIONS 预检（预检请求不带 Authorization，会被判为未登录直接 401）。
 * 本过滤器排在它前面，命中预检时直接短路返回、不再进入后续过滤器与路由，
 * 因此不需要额外改动 SaTokenConfigure；同时 CORS 响应头在进入过滤器链之前就已写入响应，
 * 后续 Sa-Token 抛出的 401 JSON 也会带上该响应头，浏览器才能读到错误体。
 */
@Configuration
public class CorsConfig {

    /**
     * 允许的前端源，Pattern 写法（如 http://localhost:*），多个用逗号分隔，* 表示不限制
     */
    @Value("${mybook.cors.allowed-origin-patterns:*}")
    private List<String> allowedOriginPatterns;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOriginPatterns(allowedOriginPatterns);
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.setMaxAge(3600L);
        // 前端通过 Authorization 请求头携带 token，因此不开启 allowCredentials（仅在需要跨源带 Cookie 时才打开）

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsWebFilter(source);
    }
}