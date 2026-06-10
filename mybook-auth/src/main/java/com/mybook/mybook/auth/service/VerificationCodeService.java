package com.mybook.mybook.auth.service;

import com.mybook.framework.common.response.Response;
import com.mybook.mybook.auth.model.vo.verificationcode.SendVerificationCodeReqVO;

public interface VerificationCodeService {

    /**
     * 发送短信验证码
     *
     * @param sendVerificationCodeReqVO
     * @return
     */
    public Response<?> send(SendVerificationCodeReqVO sendVerificationCodeReqVO);
}