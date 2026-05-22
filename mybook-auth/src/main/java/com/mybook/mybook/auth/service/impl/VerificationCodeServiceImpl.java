package com.mybook.mybook.auth.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.mybook.framework.common.exception.BizException;
import com.mybook.framework.common.response.Response;
import com.mybook.mybook.auth.constant.RedisKeyConstants;
import com.mybook.mybook.auth.enums.ResponseCodeEnum;
import com.mybook.mybook.auth.model.vo.verificationcode.SendVerificationCodeReqVO;
import com.mybook.mybook.auth.service.VerificationCodeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service  // TODO: 这个是干什么的？
@Slf4j
public class VerificationCodeServiceImpl implements VerificationCodeService {

    @Resource
    RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取手机号，检查是否已经发送校验码
     * 若已发送，返回
     * 若未发送，创建验证码并调用三方发送短信
     * @param sendVerificationCodeReqVO
     * @return
     */
    @Override
    public Response<?> send(SendVerificationCodeReqVO sendVerificationCodeReqVO) {
        String phone = sendVerificationCodeReqVO.getPhone();
        String key = RedisKeyConstants.buildVerificationCodeKey(phone);
        boolean isSent = redisTemplate.hasKey(key);
        if (isSent){
            throw new BizException(ResponseCodeEnum.VERIFICATION_CODE_SEND_FREQUENTLY);
        }
        String verificationCode = RandomUtil.randomNumbers(6);
        // todo: 三方
        log.info("==> 手机号: {}, 已发送验证码：【{}】", phone, verificationCode);
        redisTemplate.opsForValue().set(key, verificationCode, 3, TimeUnit.MINUTES);
        return Response.success();

    }
}
