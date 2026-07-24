package com.mybook.mybook.user.relation.biz.enums;


import com.mybook.framework.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ResponseCodeEnum implements BaseExceptionInterface {

    SYSTEM_ERROR("NOTE-10000", "出错啦，后台小哥正在努力修复中..."),
    PARAM_NOT_VALID("NOTE-10001", "参数错误"),

    ;

    private final String errorCode;
    private final String errorMessage;
}
