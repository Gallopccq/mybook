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
        put("data", data);
    }
    
    public static AjaxResult success() {
        return new AjaxResult(200, "操作成功");
    }

    public static AjaxResult success(Object data) {
        return new AjaxResult(200, "操作成功", data);
    }
}
