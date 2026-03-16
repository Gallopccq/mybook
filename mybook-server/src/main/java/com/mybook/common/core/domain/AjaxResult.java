package com.mybook.common.core.domain;

import java.util.HashMap;

public class AjaxResult extends HashMap<String, Object>{
    public AjaxResult() {}

    public AjaxResult(int code, String msg) {
        put("code", code);
        put("msg", msg);
    }

    public AjaxResult(int code, String msg, Object data) {
        this(code, msg);
        if (data != null) put("data", data);
    }
    
    public static AjaxResult success() {
        return new AjaxResult(200, "操作成功");
    }

    public static AjaxResult success(Object data) {
        return new AjaxResult(200, "操作成功", data);
    }

    public static AjaxResult success(String msg, Object data) {
        return new AjaxResult(200, msg, data);
    }

    public static AjaxResult warn(String msg) {
        return new AjaxResult(601, msg);
    }

    public static AjaxResult error() {
        return new AjaxResult(500, "操作失败");
    }

    public static AjaxResult error(String msg) {
        return new AjaxResult(500, msg);
    }

    public static AjaxResult error(int code, String msg) {
        return new AjaxResult(code, msg);
    }

    public static AjaxResult error(String msg, Object data) {
        return new AjaxResult(500, msg, data);
    }
    
    /** 链式调用：result.put("key", value) */
    @Override
    public AjaxResult put(String key, Object value) {
        super.put(key, value);
        return this;
}
