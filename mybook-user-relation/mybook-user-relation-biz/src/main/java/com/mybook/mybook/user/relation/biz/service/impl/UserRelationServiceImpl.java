package com.mybook.mybook.user.relation.biz.service.impl;

import com.mybook.framework.common.response.Response;
import com.mybook.mybook.user.relation.biz.model.vo.FollowUserReqVO;
import com.mybook.mybook.user.relation.biz.service.UserRelationService;

public class UserRelationServiceImpl implements UserRelationService{

    /**
     * 关注用户接口
     * 校验关注按的用户是否是自己，是否存在，关注的用户总数是否超过上限（1000）
     * 在 t_following 和 t_fans 中添加数据
     */
    @Override
    public Response<?> follow(FollowUserReqVO followUserReqVO) {
        
        return Response.success();
    }
    
}
