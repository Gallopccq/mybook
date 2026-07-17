package com.mybook.mybook.auth.constant;

public class RedisKeyConstants {
    private static final String VERIFICATION_CODE_KEY_PREFIX = "verification.code.";


    /**
     * 构建验证码 KEY
     * @param phone
     * @return
     */
    public static String buildVerificationCodeKey(String phone) {
        return VERIFICATION_CODE_KEY_PREFIX + phone;
    }
}