package com.mybook.mybook.gateway.constant;

public class RedisKeyConstants {

    private static final String USER_ROLES_KEY_PREFIX = "user:roles:";

    private static final String ROLE_PERMISSIONS_KEY_PREFIX = "role:permissions:";

    /**
     * 构建用户-角色 Key
     * @param userId
     * @return
     */
    public static String buildUserRoleKey(Long userId) {
        return USER_ROLES_KEY_PREFIX + userId;
    }


    public static String buildRolePermissionsKey(String roleKey) {
        return ROLE_PERMISSIONS_KEY_PREFIX + roleKey;
    }
}