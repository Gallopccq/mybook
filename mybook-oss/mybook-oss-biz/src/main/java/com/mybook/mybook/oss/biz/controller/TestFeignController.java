package com.mybook.mybook.oss.biz.controller;

import com.mybook.framework.biz.operationlog.aspect.ApiOperationLog;
import com.mybook.framework.common.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/file")
@Slf4j
public class TestFeignController {

    @ApiOperationLog(description = "Feign 接口测试")
    @PostMapping(value = "/test")
    public Response<?> test(){
        return Response.success();
    }
}
