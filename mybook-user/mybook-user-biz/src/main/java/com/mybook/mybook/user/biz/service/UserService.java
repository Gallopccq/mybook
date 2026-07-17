package com.mybook.mybook.user.biz.service;

import com.mybook.framework.common.response.Response;
import com.mybook.mybook.user.biz.model.vo.UpdateUserInfoReqVO;
import com.mybook.mybook.user.dto.req.FindUserByPhoneReqDTO;
import com.mybook.mybook.user.dto.req.RegisterUserReqDTO;
import com.mybook.mybook.user.dto.req.UpdateUserPasswordReqDTO;
import com.mybook.mybook.user.dto.resp.FindUserByPhoneRspDTO;

public interface UserService {
    Response<?> updateUserInfo(UpdateUserInfoReqVO updateUserInfoReqVO);
    /**
     * 用户注册
     *
     * @param registerUserReqDTO
     * @return 返回用户ID
     */
    Response<Long> register(RegisterUserReqDTO registerUserReqDTO);
    Response<FindUserByPhoneRspDTO> findByPhone(FindUserByPhoneReqDTO findUserByPhoneReqDTO);
    Response<?> updatePassword(UpdateUserPasswordReqDTO updateUserPasswordReqDTO);
}
