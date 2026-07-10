package com.mybook.mybook.user.biz.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Preconditions;
import com.mybook.framework.common.response.Response;
import com.mybook.framework.common.util.ParamUtils;
import com.mybook.mybook.user.biz.domain.dataobject.UserDO;
import com.mybook.mybook.user.biz.domain.mapper.UserDOMapper;
import com.mybook.mybook.user.biz.enums.ResponseCodeEnum;
import com.mybook.mybook.user.biz.enums.SexEnum;
import com.mybook.mybook.user.biz.filter.LoginUserContextHolder;
import com.mybook.mybook.user.biz.model.vo.UpdateUserInfoReqVO;
import com.mybook.mybook.user.biz.service.UserService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
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
        UserDO userDO = new UserDO();
        userDO.setId(LoginUserContextHolder.getUserId());
        boolean needUpdate = false;

        // avatar

        // nickname
        String nickName = updateUserInfoReqVO.getNickName();
        if (StringUtils.isNotBlank(nickName)){
            Preconditions.checkArgument(ParamUtils.checkNickname(nickName), ResponseCodeEnum.NICK_NAME_VALID_FAIL.getErrorMessage());
            userDO.setNickname(nickName);
            needUpdate = true;
        }

        // mybookId
        String mybookId = updateUserInfoReqVO.getMybookId();
        if (StringUtils.isNotBlank(mybookId)){
            Preconditions.checkArgument(ParamUtils.checkMybookId(mybookId), ResponseCodeEnum.MYBOOK_ID_VALID_FAIL.getErrorMessage());
            userDO.setMybookId(mybookId);
            needUpdate = true;
        }

        // sex
        Integer sex = updateUserInfoReqVO.getSex();
        if (Objects.nonNull(sex)){
            Preconditions.checkArgument(SexEnum.isValid(sex), ResponseCodeEnum.SEX_VALID_FAIL.getErrorMessage());
            userDO.setSex(Byte.valueOf(sex.toString()));
            needUpdate = true;
        }

        // birthday
        LocalDateTime birthday = updateUserInfoReqVO.getBirthday();
        if (Objects.nonNull(birthday)){
            userDO.setBirthday(birthday);
            needUpdate = true;
        }

        // introduction
        String introduction = updateUserInfoReqVO.getIntroduction();
        if (StringUtils.isNotBlank(introduction)){
            Preconditions.checkArgument(ParamUtils.checkLength(introduction, 100), ResponseCodeEnum.INTRODUCTION_VALID_FAIL.getErrorMessage());
            userDO.setIntroduction(introduction);
            needUpdate = true;
        }

        // background
        MultipartFile background = updateUserInfoReqVO.getBackground();
        if (Objects.nonNull(background) && background.getSize() != 0){

        }
        if (needUpdate) {
            userDO.setUpdateTime(LocalDateTime.now());
            userDOMapper.updateByPrimaryKeySelective(userDO);
        }

        return Response.success();
    }
}
