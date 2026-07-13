package com.mybook.mybook.user.biz.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Preconditions;
import com.mybook.framework.biz.context.holder.LoginUserContextHolder;
import com.mybook.framework.common.exception.BizException;
import com.mybook.framework.common.response.Response;
import com.mybook.framework.common.util.ParamUtils;
import com.mybook.mybook.oss.api.FileFeignApi;
import com.mybook.mybook.user.biz.domain.dataobject.UserDO;
import com.mybook.mybook.user.biz.domain.mapper.UserDOMapper;
import com.mybook.mybook.user.biz.enums.ResponseCodeEnum;
import com.mybook.mybook.user.biz.enums.SexEnum;
import com.mybook.mybook.user.biz.model.vo.UpdateUserInfoReqVO;
import com.mybook.mybook.user.biz.rpc.OssRpcService;
import com.mybook.mybook.user.biz.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserServiceImpl  implements UserService {
    private static final Cache<Long, Object> LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(10000)
            .maximumSize(10000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();

    @Resource
    private UserDOMapper userDOMapper;
    @Resource
    private OssRpcService ossRpcService;

    @Override
    public Response<?> updateUserInfo(UpdateUserInfoReqVO updateUserInfoReqVO) {
        UserDO userDO = new UserDO();
        userDO.setId(LoginUserContextHolder.getUserId());
        boolean needUpdate = false;

        // avatar
        MultipartFile avatar = updateUserInfoReqVO.getAvatar();
        if (Objects.nonNull(avatar)){
            String avatarUrl = ossRpcService.uploadFile(avatar);
            log.info("==> 调用 oss 服务成功，上传头像，url：{}", avatarUrl);
            if (StringUtils.isBlank(avatarUrl)){
                throw new BizException(ResponseCodeEnum.UPLOAD_AVATAR_FAIL);
            }
            userDO.setAvatar(avatarUrl);
            needUpdate = true;
        }

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
        LocalDate birthday = updateUserInfoReqVO.getBirthday();
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
            String backgroundUrl = ossRpcService.uploadFile(background);
            log.info("==> 调用 oss 服务成功，上传背景图，url：{}", backgroundUrl);
            if (StringUtils.isBlank(backgroundUrl)){
                throw new BizException(ResponseCodeEnum.UPLOAD_BACKGROUND_IMG_FAIL);
            }
            userDO.setBackgroundImg(backgroundUrl);
            needUpdate = true;

        }
        if (needUpdate) {
            userDO.setUpdateTime(LocalDateTime.now());
            userDOMapper.updateByPrimaryKeySelective(userDO);
        }


        return Response.success();
    }
}
