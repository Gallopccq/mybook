package com.mybook.mybook.user.relation.biz.service;

import com.mybook.framework.common.response.PageResponse;
import com.mybook.framework.common.response.Response;
import com.mybook.mybook.user.relation.biz.model.vo.*;

public interface UserRelationService {
    
    Response<?> follow(FollowUserReqVO followUserReqVO);

    Response<?> unfollow(UnfollowUserReqVO unfollowUserReqVO);

    PageResponse<FindFollowingListRspVO> findFollowingList(FindFollowingListReqVO findFollowingListReqVO);

    /**
     * 查询粉丝列表
     * @param findFansListReqVO
     * @return
     */
    PageResponse<FindFansUserRspVO> findFansList(FindFansListReqVO findFansListReqVO);
}
