package com.mybook.mybook.auth.constant;

public class RedisKeyConstants {
    private static final String ROLE_PERMISSION_KEY_PREFIX = "role:permissions:";
    private static final String VERIFICATION_CODE_KEY_PREFIX = "verification_code:";

    public static String buildRolePermissionsKey(Long roleId) {
        return ROLE_PERMISSION_KEY_PREFIX + roleId;
    }

    /**
     * 构建验证码 KEY
     * @param phone
     * @return
     */
    public static String buildVerificationCodeKey(String phone) {
        return VERIFICATION_CODE_KEY_PREFIX + phone;
    }
}