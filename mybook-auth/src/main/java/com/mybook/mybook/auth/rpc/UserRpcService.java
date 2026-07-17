package com.mybook.mybook.auth.rpc;

import com.mybook.framework.common.response.Response;
import com.mybook.mybook.user.api.UserFeignApi;
import com.mybook.mybook.user.dto.req.FindUserByPhoneReqDTO;
import com.mybook.mybook.user.dto.req.RegisterUserReqDTO;
import com.mybook.mybook.user.dto.req.UpdateUserPasswordReqDTO;
import com.mybook.mybook.user.dto.resp.FindUserByPhoneRspDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class UserRpcService {
    @Resource
    private UserFeignApi userFeignApi;

    public Long registerUser(String phone){
        RegisterUserReqDTO registerUserReqDTO = new RegisterUserReqDTO();
        registerUserReqDTO.setPhone(phone);
        Response<Long> response = userFeignApi.registerUser(registerUserReqDTO);
        if (!response.isSuccess()){
            return null;
        }
        return response.getData();
    }

    public FindUserByPhoneRspDTO findByPhone(String phone){
        FindUserByPhoneReqDTO reqDTO = new FindUserByPhoneReqDTO();
        reqDTO.setPhone(phone);
        Response<FindUserByPhoneRspDTO> response = userFeignApi.fingByPhone(reqDTO);
        if (!response.isSuccess()){
            return null;
        }
        return response.getData();
    }

    public Response<?> updatePassword(String encodePassword){
        UpdateUserPasswordReqDTO reqDTO = new UpdateUserPasswordReqDTO();
        reqDTO.setEncodePassword(encodePassword);
        return userFeignApi.updatePassword(reqDTO);
    }
}
