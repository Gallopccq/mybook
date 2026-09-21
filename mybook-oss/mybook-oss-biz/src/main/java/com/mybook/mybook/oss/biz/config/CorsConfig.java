package com.mybook.mybook.oss.biz.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 跨域配置：mybook-oss 没有网关路由，前端是直连上传接口的，跨域只能在本服务放开。
 *
 * 只放开文件相关接口，不要整站放开。上传走的是 multipart/form-data（简单请求，通常不触发预检），
 * 但响应必须带上 Access-Control-Allow-Origin，前端才读得到返回的文件地址。
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 允许的前端源，Pattern 写法（如 http://localhost:*），多个用逗号分隔，* 表示不限制
     */
    @Value("${mybook.cors.allowed-origin-patterns:*}")
    private List<String> allowedOriginPatterns;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/file/**")
                .allowedOriginPatterns(allowedOriginPatterns.toArray(new String[0]))
                .allowedMethods("*")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}