package com.mybook.mybook.note.biz.exception;

import com.mybook.framework.common.exception.BizException;
import com.mybook.framework.common.response.Response;
import com.mybook.mybook.note.biz.enums.ResponseCodeEnum;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

/**
 * ControllerAdvice用于对所有controller层（有controller注解的）进行处理
 * 配合@ExceptionHandler进行全局异常处理
 * 配合@InitBinder，设置WebDataBinder，进行全局请求数据预处理（多用于表单提交，url传参等）
 * 配合@ModelAttribute进行全局数据绑定，让Controller类中所有的方法都可以获取到通过@ModelAttribute注解设置的值
 */
@Slf4j
@ControllerAdvice

public class GlobalExceptionHandler {

    /**
     * 捕获自定义业务异常
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler({BizException.class})
    @ResponseBody
    public Response<Object> handleBizException(HttpServletRequest request, BizException e) {
        log.warn("{} request fail, errorCode: {}, errorMessage: {}", request.getRequestURI(), e.getErrorCode(), e.getErrorMessage());
        return Response.fail(e);
    }

    /**
     * 捕获参数校验异常
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseBody
    public Response<Object> handleMethodArgumentNotValidException(HttpServletRequest request, MethodArgumentNotValidException e) {
        String errorCode = ResponseCodeEnum.PARAM_NOT_VALID.getErrorCode();
        BindingResult bindingResult = e.getBindingResult(); // todo: bindingresult 什么用
        StringBuilder sb = new StringBuilder();

        Optional.ofNullable(bindingResult.getFieldErrors()).ifPresent(errors -> {
            errors.forEach(error ->
               sb.append(error.getField())
                       .append(" ")
                       .append(error.getDefaultMessage())
                       .append(", 当前值: '")
                       .append(error.getRejectedValue())
                       .append("'; ")
            );
        });
        String errorMessage = sb.toString();
        log.warn("{} request error, errorCode: {}, errorMessage: {}", request.getRequestURI(), errorCode, errorMessage);
        return Response.fail(errorCode, errorMessage);
    }
    /**
     * 捕获 guava 参数校验异常
     * @return
     */
    @ExceptionHandler({ IllegalArgumentException.class })
    @ResponseBody
    public Response<Object> handleIllegalArgumentException(HttpServletRequest request, IllegalArgumentException e) {
        // 参数错误异常码
        String errorCode = ResponseCodeEnum.PARAM_NOT_VALID.getErrorCode();

        // 错误信息
        String errorMessage = e.getMessage();

        log.warn("{} request error, errorCode: {}, errorMessage: {}", request.getRequestURI(), errorCode, errorMessage);

        return Response.fail(errorCode, errorMessage);
    }

    @ExceptionHandler({Exception.class})
    @ResponseBody
    public Response<Object> handleOtherException(HttpServletRequest request, Exception e) {
        log.warn("{} request error, ", request.getRequestURI(), e);
        return Response.fail(ResponseCodeEnum.SYSTEM_ERROR);
    }

}