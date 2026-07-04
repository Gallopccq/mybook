package com.mybook.mybook.auth.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.google.common.collect.Lists;
import com.mybook.framework.common.enums.DeletedEnum;
import com.mybook.framework.common.enums.StatusEnum;
import com.mybook.framework.common.exception.BizException;
import com.mybook.framework.common.response.Response;
import com.mybook.framework.common.util.JsonUtils;
import com.mybook.mybook.auth.constant.RedisKeyConstants;
import com.mybook.mybook.auth.constant.RoleConstants;
import com.mybook.mybook.auth.domain.dataobject.RoleDO;
import com.mybook.mybook.auth.domain.dataobject.UserDO;
import com.mybook.mybook.auth.domain.mapper.RoleDOMapper;
import com.mybook.mybook.auth.domain.mapper.UserDOMapper;
import com.mybook.mybook.auth.enums.LoginTypeEnum;
import com.mybook.mybook.auth.enums.ResponseCodeEnum;
import com.mybook.mybook.auth.model.vo.user.UserLoginReqVO;
import com.mybook.mybook.auth.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 若密码登录，则通过数据库校验密码是否正确
 * 若验证码登录，则校验验证码是否正确，若正确，则登录或为未注册用户自动注册，然后通过satoken获取token并返回。
 * 对接服务：mysql，redis
 *
 * 接口地址：/usr/login
 * 入参
 * {
 *     "phone": "18011119108", // 手机号
 *     "code": "218603", // 登录验证码，验证码登录时，需要填写
 *     "password": "xx", // 密码登录时，需要填写
 *     "type": 1 // 登录类型，1表示手机号验证码登录；2表示账号密码登录
 * }
 * 出参
 * {
 * 	"success": true,
 * 	"message": null,
 * 	"errorCode": null,
 * 	"data": "xxxxx" // 登录成功后，返回 Token 令牌
 * }
 *
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Resource
    private UserDOMapper userDOMapper;

    @Resource
    private RoleDOMapper roleDOMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Response<String> loginAndRegister(UserLoginReqVO userLoginReqVO) {
        String phone = userLoginReqVO.getPhone();
        Integer type = userLoginReqVO.getType();

        LoginTypeEnum loginTypeEnum = LoginTypeEnum.valueOf(type);
        Long userId = null;

        // 判断登录类型
        switch (loginTypeEnum) {
            case VERIFICATION_CODE:
                String verificationCode = userLoginReqVO.getCode();

                if (StringUtils.isBlank(verificationCode)){
                    return Response.fail(ResponseCodeEnum.PARAM_NOT_VALID.getErrorCode(), "验证码不能为空");
                }

                // 构建redis key
                String key = RedisKeyConstants.buildVerificationCodeKey(phone);
                String sendCode = (String)redisTemplate.opsForValue().get(key);
//                log.info(redisTemplate.getKeySerializer().toString());

                // 判断key是否匹配
                if (!StringUtils.equals(verificationCode, sendCode)) {
                    // todo: 为什么上面是返回Response，这里是抛异常
//                    log.debug(key);
                    throw new BizException(ResponseCodeEnum.VERIFICATION_CODE_ERROR);
                }

                // 数据库查手机号对应用户信息
                UserDO userDO = userDOMapper.selectByPhone(phone);
                // 判断用户是否存在
                log.info("==> 用户是否注册, phone: {}, userDO: {}", phone, JsonUtils.toJsonString(userDO));

                if (Objects.isNull(userDO)) {
                    // 未注册
                    userId = registerUser(phone);
                } else {
                    // 已注册
                    userId = userDO.getId();
                }

                break;
            case PASSWORD:
                // todo
                break;
            default:
                break;
        }
        // SaToken 登录用户，并返回token令牌
        StpUtil.login(userId);
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        return Response.success(tokenInfo.tokenValue);
    }

    public Long registerUser(String phone) {
        Long userId = redisTemplate.opsForValue().increment(RedisKeyConstants.MYBOOK_ID_GENERATOR_KEY);

        UserDO userDO = UserDO.builder()
                .phone(phone)
                .mybookId(String.valueOf(userId))
                .nickname("小红薯"+userId)
                .status(StatusEnum.ENABLE.getValue().byteValue())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(DeletedEnum.NO.getValue())
                .build();
        userDOMapper.insert(userDO);

        // 给用户分配默认角色
        RoleDO roleDO = roleDOMapper.selectByPrimaryKey(RoleConstants.COMMON_USER_ROLE_ID);
        List<String> roles = new ArrayList<String>(1);
        roles.add(roleDO.getRoleKey());
        String userRolesKey = RedisKeyConstants.buildUserRoleKey(userId);
        redisTemplate.opsForValue().set(userRolesKey, JsonUtils.toJsonString(roles));

        // todo 是否要同步更新 mysql

        return userId;
    }

    @Override
    public Response<String> logout(Long userId) {
        StpUtil.logout(userId);
        return Response.success();
    }
}
