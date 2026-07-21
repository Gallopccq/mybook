package com.mybook.mybook.user.biz.constant;

public class RedisKeyConstants {

    public static final String MYBOOK_ID_GENERATOR_KEY = "mybook.id.generator";

    private static final String USER_ROLES_KEY_PREFIX = "user:roles:";

    private static final String ROLE_PERMISSIONS_KEY_PREFIX = "role:permissions:";

    private static final String USER_INFO_KEY_PREFIX = "user:info:";

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

    public static String buildUserInfoKey(Long id){
        return USER_INFO_KEY_PREFIX + id;
    }
}