package com.mybook.mybook.gateway.auth;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.naming.NoPermissionException;

@Configuration
public class SaTokenConfigure {
    // 注册SaToken全局过滤器
    @Bean
    public SaReactorFilter getSaReactorFilter(){
        return new SaReactorFilter()
                // 拦截地址
                .addInclude("/**")
                // 开放地址
//                .addExclude("/favicon.ico")
                // 鉴权方法: 每次访问进入
                .setAuth(obj -> {
                    SaRouter.match("/**")
                            .notMatch("/auth/verification/code/send")
                            .notMatch("/auth/user/login")
                            .check(r -> StpUtil.checkLogin());
                    SaRouter.match("/auth/user/logout")
                            .check(r -> StpUtil.checkRole("admin"));
                })
                .setError(e -> {
                    if (e instanceof NotLoginException) {
                        throw new NotLoginException(e.getMessage(), null, null);
                    } else if (e instanceof NotPermissionException) {
                        throw new NotPermissionException(e.getMessage());
                    } else {
                        throw new SaTokenException(e.getMessage());
                    }
                })
                ;
    }
}
