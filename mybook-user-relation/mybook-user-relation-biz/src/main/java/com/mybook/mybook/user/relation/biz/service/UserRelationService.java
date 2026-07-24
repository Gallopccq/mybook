package com.mybook.mybook.user.relation.biz.service;

import com.mybook.framework.common.response.Response;
import com.mybook.mybook.user.relation.biz.model.vo.FollowUserReqVO;

public interface UserRelationService {
    
    Response<?> follow(FollowUserReqVO followUserReqVO);
}
