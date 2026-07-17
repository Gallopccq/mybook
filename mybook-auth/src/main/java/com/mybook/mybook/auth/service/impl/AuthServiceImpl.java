package com.mybook.mybook.auth.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.mybook.framework.common.exception.BizException;
import com.mybook.framework.common.response.Response;
import com.mybook.mybook.auth.constant.RedisKeyConstants;
import com.mybook.mybook.auth.enums.LoginTypeEnum;
import com.mybook.mybook.auth.enums.ResponseCodeEnum;
import com.mybook.mybook.auth.filter.LoginUserContextHolder;
import com.mybook.mybook.auth.model.vo.user.UpdatePasswordReqVO;
import com.mybook.mybook.auth.model.vo.user.UserLoginReqVO;
import com.mybook.mybook.auth.rpc.UserRpcService;
import com.mybook.mybook.auth.service.AuthService;
import com.mybook.mybook.user.dto.resp.FindUserByPhoneRspDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
public class AuthServiceImpl implements AuthService {
    @Resource
    private UserRpcService userRpcService;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Override
    public Response<String> loginAndRegister(UserLoginReqVO userLoginReqVO) {
        String phone = userLoginReqVO.getPhone();
        Integer type = userLoginReqVO.getType();

        LoginTypeEnum loginTypeEnum = LoginTypeEnum.valueOf(type);
        if (Objects.isNull(loginTypeEnum)){
            throw new BizException(ResponseCodeEnum.LOGIN_TYPE_ERROR);
        }

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
                    // 答：未实现的功能，上面是功能校验参数的一部分
                    throw new BizException(ResponseCodeEnum.VERIFICATION_CODE_ERROR);
                }

                Long userIdTmp = userRpcService.registerUser(phone);
                if (Objects.isNull(userIdTmp)){
                    throw new BizException(ResponseCodeEnum.LOGIN_FAIL);
                }
                userId = userIdTmp;


                break;
            case PASSWORD:
                FindUserByPhoneRspDTO rspDTO = userRpcService.findByPhone(phone);
                if (Objects.isNull(rspDTO)) {
                    throw new BizException(ResponseCodeEnum.USER_NOT_FOUND);
                }

                String password = userLoginReqVO.getPassword();

                String encodePassword = rspDTO.getPassword();
                boolean isPasswordCorrect = passwordEncoder.matches(password, encodePassword);
                if (!isPasswordCorrect){
                    throw new BizException(ResponseCodeEnum.PHONE_OR_PASSWORD_ERROR);
                }

                userId = rspDTO.getId();
                break;
            default:
                break;
        }
        // SaToken 登录用户，并返回token令牌
        StpUtil.login(userId);
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        return Response.success(tokenInfo.tokenValue);
    }

    @Override
    public Response<String> logout() {
        Long userId = LoginUserContextHolder.getUserId();

        log.info("==> 用户退出登录, userId: {}", userId);

        threadPoolTaskExecutor.submit(() -> {
            Long userId2 = LoginUserContextHolder.getUserId();
            log.info("==> 异步线程中获取 userId: {}", userId2);
        });

        StpUtil.logout(userId);
        return Response.success();
    }

    @Override
    public Response updatePassword(UpdatePasswordReqVO updatePasswordReqVO) {
        String newPassword = updatePasswordReqVO.getNewPassword();
        String encodePassword = passwordEncoder.encode(newPassword);

        Response<?> response = userRpcService.updatePassword(encodePassword);

        return Response.success();
    }
}
