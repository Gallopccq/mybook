package com.mybook.mybook.note.biz.rpc;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.mybook.framework.common.exception.BizException;
import com.mybook.framework.common.response.Response;
import com.mybook.mybook.note.biz.enums.ResponseCodeEnum;
import com.mybook.mybook.user.api.UserFeignApi;
import com.mybook.mybook.user.dto.req.FindUserByIdReqDTO;
import com.mybook.mybook.user.dto.resp.FindUserByIdRspDTO;

@Service
public class UserRpcService {
    private UserFeignApi userFeignApi;

    public FindUserByIdRspDTO findById(Long id){
        FindUserByIdReqDTO findUserByIdReqDTO = FindUserByIdReqDTO.builder().id(id).build();
        Response<FindUserByIdRspDTO> response = userFeignApi.findById(findUserByIdReqDTO);
        if (Objects.isNull(response) || !response.isSuccess()){
            return null;
        }
        
        return response.getData();

    }
}
