package com.mybook.mybook.user.biz.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mybook.framework.common.response.Response;
import com.mybook.mybook.user.biz.domain.mapper.UserDOMapper;
import com.mybook.mybook.user.biz.model.vo.UpdateUserInfoReqVO;
import com.mybook.mybook.user.biz.service.UserService;
import jakarta.annotation.Resource;

import java.util.concurrent.TimeUnit;

public class UserServiceImpl  implements UserService {
    private static final Cache<Long, Object> LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(10000)
            .maximumSize(10000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();

    @Resource
    private UserDOMapper userDOMapper;
    @Override
    public Response<?> updateUserInfo(UpdateUserInfoReqVO updateUserInfoReqVO) {

        return null;
    }
}
