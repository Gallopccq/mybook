package com.mybook.mybook.auth.constant;

public class RedisKeyConstants {
    private static final String VERIFICATION_CODE_KEY_PREFIX = "verification.code:";

    public static final String MYBOOK_ID_GENERATOR_KEY = "mybook.id.generator";

    private static final String USER_ROLES_KEY_PREFIX = "user:roles:";

    private static final String ROLE_PERMISSIONS_KEY_PREFIX = "role:permissions:";

    /**
     * 构建用户-角色 Key
     * @param phone
     * @return
     */
    public static String buildUserRoleKey(String phone) {
        return USER_ROLES_KEY_PREFIX + phone;
    }


    public static String buildRolePermissionsKey(Long roleId) {
        return ROLE_PERMISSIONS_KEY_PREFIX + roleId;
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