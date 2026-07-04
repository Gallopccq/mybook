package com.mybook.mybook.gateway.enums;

import com.mybook.framework.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCodeEnum implements BaseExceptionInterface {
    SYSTEM_ERROR("500", "系统繁忙，请稍后重试"),
    UNAUTHORIZED("401", "权限不足"),
    NOT_LOGIN("402", "未登录或登陆过期"),

        ;

    private final String errorCode;
    private final String errorMessage;

}
