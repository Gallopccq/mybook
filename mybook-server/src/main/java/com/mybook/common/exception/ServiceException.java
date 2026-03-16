package com.mybook.common.exception;

public final class ServiceException extends  RuntimeException{
    private Integer code;
    private String message;

    public ServiceException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public ServiceException(String message) {
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }
    @Override
    public String getMessage() {
        return message;
    }
}
