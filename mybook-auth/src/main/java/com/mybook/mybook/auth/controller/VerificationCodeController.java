package com.mybook.mybook.auth.controller;

import com.mybook.framework.biz.operationlog.aspect.ApiOperationLog;
import com.mybook.framework.common.response.Response;
import com.mybook.mybook.auth.model.vo.verificationcode.SendVerificationCodeReqVO;
import com.mybook.mybook.auth.service.VerificationCodeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 验证码获取和验证接口
 * 以json格式返回
 */

@Controller
@Slf4j
public class VerificationCodeController {
    @Resource
    private VerificationCodeService verificationCodeService;

    @ResponseBody
    @PostMapping("/verification/code/send")
    @ApiOperationLog(description = "发送短信验证码")
    public Response<?> send(@Validated @RequestBody SendVerificationCodeReqVO sendVerificationCodeReqVO) {
        return verificationCodeService.send(sendVerificationCodeReqVO);
    }


}