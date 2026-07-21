package com.mybook.mybook.note.biz.enums;

import com.mybook.framework.common.exception.BaseExceptionInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ResponseCodeEnum implements BaseExceptionInterface{
    SYSTEM_ERROR("NOTE-10000", "出错啦，后台小哥正在努力修复中..."),
    PARAM_NOT_VALID("NOTE-10001", "参数错误"),
    USER_NOT_FOUND("NOTE-10002", "用户未登录"),
    // ----------- 业务异常状态码 -----------
    NOTE_TYPE_ERROR("NOTE-20000", "未知的笔记类型"),
    NOTE_PUBLISH_FAIL("NOTE-20001", "笔记发布失败"),
    
    ;

    private final String errorCode;
    private final String errorMessage;
    
}
