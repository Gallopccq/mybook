package com.mybook.mybook.user.biz.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Preconditions;
import com.mybook.framework.biz.context.holder.LoginUserContextHolder;
import com.mybook.framework.common.enums.DeletedEnum;
import com.mybook.framework.common.enums.StatusEnum;
import com.mybook.framework.common.exception.BizException;
import com.mybook.framework.common.response.Response;
import com.mybook.framework.common.util.JsonUtils;
import com.mybook.framework.common.util.ParamUtils;
import com.mybook.mybook.user.biz.constant.RedisKeyConstants;
import com.mybook.mybook.user.biz.constant.RoleConstants;
import com.mybook.mybook.user.biz.domain.dataobject.RoleDO;
import com.mybook.mybook.user.biz.domain.dataobject.UserDO;
import com.mybook.mybook.user.biz.domain.dataobject.UserRoleDO;
import com.mybook.mybook.user.biz.domain.mapper.RoleDOMapper;
import com.mybook.mybook.user.biz.domain.mapper.UserDOMapper;
import com.mybook.mybook.user.biz.domain.mapper.UserRoleDOMapper;
import com.mybook.mybook.user.biz.enums.ResponseCodeEnum;
import com.mybook.mybook.user.biz.enums.SexEnum;
import com.mybook.mybook.user.biz.model.vo.UpdateUserInfoReqVO;
import com.mybook.mybook.user.biz.rpc.DistributedIdGeneratorRpcService;
import com.mybook.mybook.user.biz.rpc.OssRpcService;
import com.mybook.mybook.user.biz.service.UserService;
import com.mybook.mybook.user.dto.req.FindUserByPhoneReqDTO;
import com.mybook.mybook.user.dto.req.RegisterUserReqDTO;
import com.mybook.mybook.user.dto.req.UpdateUserPasswordReqDTO;
import com.mybook.mybook.user.dto.resp.FindUserByPhoneRspDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    @Resource
    private DistributedIdGeneratorRpcService distributedIdGeneratorRpcService;
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private UserRoleDOMapper userRoleDOMapper;
    @Resource
    private RoleDOMapper roleDOMapper;


    @Override
    public Response<?> updatePassword(UpdateUserPasswordReqDTO updateUserPasswordReqDTO) {
        Long userId = LoginUserContextHolder.getUserId();
        UserDO userDO = UserDO.builder()
                .id(userId)
                .password(updateUserPasswordReqDTO.getEncodePassword())
                .updateTime(LocalDateTime.now())
                .build();
        userDOMapper.updateByPrimaryKeySelective(userDO);
        return Response.success();
    }

    @Override
    public Response<FindUserByPhoneRspDTO> findByPhone(FindUserByPhoneReqDTO findUserByPhoneReqDTO) {
        String phone = findUserByPhoneReqDTO.getPhone();
        UserDO userDO = userDOMapper.selectByPhone(phone);
        if (Objects.isNull(userDO)){
            throw new BizException(ResponseCodeEnum.USER_NOT_FOUND);
        }
        Long userId = userDO.getId();
        String password = userDO.getPassword();

        return Response.success(new FindUserByPhoneRspDTO(userId, password));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Response<Long> register(RegisterUserReqDTO registerUserReqDTO) {
        String phone = registerUserReqDTO.getPhone();

        UserDO userDO1 = userDOMapper.selectByPhone(phone);
        log.info("==> 用户是否注册, phone: {}, userDO: {}", phone, JsonUtils.toJsonString(userDO1));

        if (Objects.nonNull(userDO1)){
            return Response.success(userDO1.getId());
        }

        String mybookId = distributedIdGeneratorRpcService.getMybookId();
        Long userId = Long.valueOf(distributedIdGeneratorRpcService.getUserId());

        UserDO userDO = UserDO.builder()
                .id(userId)
                .phone(phone)
                .mybookId(mybookId)
                .nickname("小红薯"+mybookId)
                .status(StatusEnum.ENABLE.getValue().byteValue())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(DeletedEnum.NO.getValue())
                .build();
        userDOMapper.insert(userDO);

        userId = userDO.getId();

        UserRoleDO userRoleDO = UserRoleDO.builder()
                .userId(userId)
                .roleId(RoleConstants.COMMON_USER_ROLE_ID)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        userRoleDOMapper.insertSelective(userRoleDO);
        RoleDO roleDO = roleDOMapper.selectByPrimaryKey(userRoleDO.getRoleId());
        List<String> roles = new ArrayList<>(1);
        roles.add(roleDO.getRoleKey());

        String userRolesKey = RedisKeyConstants.buildUserRoleKey(userId);
        redisTemplate.opsForValue().set(userRolesKey, JsonUtils.toJsonString(roles));

        return Response.success(userId);
    }

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
