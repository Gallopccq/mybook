package com.mybook.mybook.user.biz.service;

import com.mybook.framework.common.response.Response;
import com.mybook.mybook.user.biz.model.vo.UpdateUserInfoReqVO;

public interface UserService {
    Response<?> updateUserInfo(UpdateUserInfoReqVO updateUserInfoReqVO);
}
