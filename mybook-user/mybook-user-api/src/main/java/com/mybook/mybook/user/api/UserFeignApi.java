package com.mybook.mybook.user.api;

import com.mybook.framework.common.response.Response;
import com.mybook.mybook.user.constant.ApiConstants;
import com.mybook.mybook.user.dto.req.FindUserByIdReqDTO;
import com.mybook.mybook.user.dto.req.FindUserByPhoneReqDTO;
import com.mybook.mybook.user.dto.req.RegisterUserReqDTO;
import com.mybook.mybook.user.dto.req.UpdateUserPasswordReqDTO;
import com.mybook.mybook.user.dto.resp.FindUserByIdRspDTO;
import com.mybook.mybook.user.dto.resp.FindUserByPhoneRspDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface UserFeignApi {
    String PREFIX = "";

    @PostMapping(value = PREFIX + "/register")
    Response<Long> registerUser(@RequestBody RegisterUserReqDTO registerUserReqDTO);

    @PostMapping(value = PREFIX + "/findByPhone")
    Response<FindUserByPhoneRspDTO> fingByPhone(@RequestBody FindUserByPhoneReqDTO reqDTO);

    @PostMapping(value = PREFIX + "/password/update")
    Response<?> updatePassword(@RequestBody UpdateUserPasswordReqDTO updateUserPasswordReqDTO);

    @PostMapping(value = PREFIX + "/findById")
    Response<FindUserByIdRspDTO> findById(@RequestBody FindUserByIdReqDTO findUserByIdReqDTO);

    @PostMapping(value = PREFIX + "/findByIdWithRedis")
    Response<FindUserByIdRspDTO> findByIdWithRedis(@RequestBody FindUserByIdReqDTO findUserByIdReqDTO);

    @PostMapping(value = PREFIX + "/findByIdWithDatabase")
    Response<FindUserByIdRspDTO> findByIdWithDatabase(@RequestBody FindUserByIdReqDTO findUserByIdReqDTO);
}

