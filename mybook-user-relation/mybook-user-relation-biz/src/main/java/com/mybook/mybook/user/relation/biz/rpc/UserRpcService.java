package com.mybook.mybook.user.relation.biz.rpc;

import cn.hutool.core.collection.CollUtil;
import com.mybook.framework.common.response.Response;
import com.mybook.mybook.user.api.UserFeignApi;
import com.mybook.mybook.user.dto.req.FindUserByIdReqDTO;
import com.mybook.mybook.user.dto.req.FindUsersByIdsReqDTO;
import com.mybook.mybook.user.dto.resp.FindUserByIdRspDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class UserRpcService {

    @Resource
    private UserFeignApi userFeignApi;

    public FindUserByIdRspDTO findById(Long userId){
        FindUserByIdReqDTO findUserByIdReqDTO = FindUserByIdReqDTO.builder().id(userId).build();
        Response<FindUserByIdRspDTO> response = userFeignApi.findById(findUserByIdReqDTO);
        if (Objects.isNull(response) || !response.isSuccess() || Objects.isNull(response.getData())){
            return null;
        }
        return response.getData();
    }

    public List<FindUserByIdRspDTO> findByIds(List<Long> ids){
        FindUsersByIdsReqDTO findUsersByIdsReqDTO = FindUsersByIdsReqDTO.builder()
                .ids(ids)
                .build();
        Response<List<FindUserByIdRspDTO>> response = userFeignApi.findByIds(findUsersByIdsReqDTO);
        if (Objects.isNull(response) || !response.isSuccess() || CollUtil.isEmpty(response.getData())){
            return null;
        }
        return response.getData();
    }
}
