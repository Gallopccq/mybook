package com.mybook.common.core.page;

import java.util.List;

public class TableDataInfo {
    private long total;
    private List<?> rows;
    private int code;
    private String msg;

    public TableDataInfo() {}

    public TableDataInfo(List<?> rows, long total) {
        this.rows = rows;
        this.total = total;
        this.code = 200;
        this.msg = "查询成功";
    }

    public static TableDataInfo of(List<?> list, long total) {
        return new TableDataInfo(list, total);
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<?> getRows() {
        return rows;
    }

    public void setRows(List<?> rows) {
        this.rows = rows;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }


    
}
