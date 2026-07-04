package com.mybook.mybook.gateway.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.SaTokenException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybook.framework.common.response.Response;
import com.mybook.mybook.gateway.enums.ResponseCodeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {
    @Resource
    ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        log.error("全局异常捕获: ", ex);

        // 响应参数
        Response<?> result;
        if (ex instanceof NotLoginException){
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            result = Response.fail(ResponseCodeEnum.NOT_LOGIN);
            log.error("goto NotLoginException");
        } else if (ex instanceof SaTokenException) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            result = Response.fail(ResponseCodeEnum.UNAUTHORIZED);
            log.error("goto SaTokenException");
        } else {
            result = Response.fail(ResponseCodeEnum.SYSTEM_ERROR);
        }

        response.getHeaders().setContentType(MediaType.APPLICATION_JSON_UTF8);
        return response.writeWith(Mono.fromSupplier(() -> {  // 使用 Mono.fromSupplier 创建响应体
            DataBufferFactory bufferFactory = response.bufferFactory();
            try {
                // 使用 ObjectMapper 将 result 对象转换为 JSON 字节数组
                return bufferFactory.wrap(objectMapper.writeValueAsBytes(result));
            } catch (Exception e){
                // 如果转换过程中出现异常，则返回空字节数组
                return bufferFactory.wrap(new byte[0]);
            }
        }));
    }
}
