package com.mybook.server.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.mybook.framework.web.interceptor.LoginTokenInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer{
    private final LoginTokenInterceptor loginTokenInterceptor;

    public WebMvcConfig(LoginTokenInterceptor loginTokenInterceptor) {
        this.loginTokenInterceptor = loginTokenInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginTokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/login",
                    "/error",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                );
        // 这是干什么的？对后续扩充api会有影响吗？
    }

    
}
