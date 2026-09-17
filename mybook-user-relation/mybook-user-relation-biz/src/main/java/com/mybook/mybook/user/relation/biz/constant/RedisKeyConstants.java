package com.mybook.mybook.user.relation.biz.constant;

public class RedisKeyConstants {

    public static final String USER_FOLLOWING_KEY_PREFIX = "following:";

    /**
     * 粉丝列表 KEY 前缀
     */
    private static final String USER_FANS_KEY_PREFIX = "fans:";


    public static String buildFollowingKey(Long userId) {
        return USER_FOLLOWING_KEY_PREFIX + userId;
    }

    public static String buildUserFansKey(Long userId){
        return USER_FANS_KEY_PREFIX + userId;
    }



}