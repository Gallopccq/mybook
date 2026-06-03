package com.mybook.mybook.auth.service;

import com.mybook.framework.common.response.Response;
import com.mybook.mybook.auth.model.vo.verificationcode.SendVerificationCodeReqVO;

public interface VerificationCodeService {
    public Response<?> send(SendVerificationCodeReqVO sendVerificationCodeReqVO);
}