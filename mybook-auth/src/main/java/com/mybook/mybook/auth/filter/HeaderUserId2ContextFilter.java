package com.mybook.mybook.auth.filter;

import com.mybook.framework.common.constant.GlobalConstants;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class HeaderUserId2ContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, jakarta.servlet.FilterChain filterChain) throws ServletException, IOException {
        String userId = request.getHeader(GlobalConstants.USER_ID);
        log.info("## HeaderUserId2ContextFilter, 用户 ID: {}", userId);
        if (StringUtils.isBlank(userId)){
            filterChain.doFilter(request, response);
            return;
        }
        log.info("===== 设置 userId 到 ThreadLocal 中， 用户 ID: {}", userId);
        LoginUserContextHolder.setUserId(userId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            LoginUserContextHolder.remove();
            log.info("===== 删除 ThreadLocal， userId: {}", userId);
        }

    }
}
