package com.mybook.framework.web.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.mybook.framework.web.annotation.NoAuth;
import com.mybook.framework.web.service.TokenService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginTokenInterceptor implements HandlerInterceptor{
    private final TokenService tokenService;

    public LoginTokenInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod hm)) return true;

        if (hm.getMethod().isAnnotationPresent(NoAuth.class) ||
            hm.getBeanType().isAnnotationPresent(NoAuth.class)) {
                return true;
            }
        
        String jwt = tokenService.resolveJwtFromRequest(request)
                    .orElseThrow(() -> new IllegalStateException("未登录"));
        if (tokenService.parseLoginUser(jwt).isEmpty()){
            throw new IllegalStateException("登录已过期");
        }
        return true;
    }

    
}
