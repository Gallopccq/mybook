package com.mybook.mybook.auth.service;

import com.mybook.framework.common.response.Response;
import com.mybook.mybook.auth.model.vo.user.UpdatePasswordReqVO;
import com.mybook.mybook.auth.model.vo.user.UserLoginReqVO;

public interface AuthService {

    /**
     * 登录与注册
     * @param userLoginReqVO
     * @return
     */
    Response<String> loginAndRegister(UserLoginReqVO userLoginReqVO);
    Response<String> logout();

    Response updatePassword(UpdatePasswordReqVO updatePasswordReqVO);
}
