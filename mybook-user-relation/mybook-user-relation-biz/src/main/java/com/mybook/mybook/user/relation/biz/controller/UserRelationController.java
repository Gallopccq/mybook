package com.mybook.mybook.user.relation.biz.controller;

import com.mybook.framework.biz.operationlog.aspect.ApiOperationLog;
import com.mybook.framework.common.response.PageResponse;
import com.mybook.framework.common.response.Response;
import com.mybook.mybook.user.relation.biz.model.vo.FindFollowingListReqVO;
import com.mybook.mybook.user.relation.biz.model.vo.FindFollowingListRspVO;
import com.mybook.mybook.user.relation.biz.model.vo.FollowUserReqVO;
import com.mybook.mybook.user.relation.biz.service.UserRelationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/relation")
@RestController
@Slf4j
public class UserRelationController {
    @Resource
    private UserRelationService userRelationService;

    @PostMapping("/follow")
    @ApiOperationLog(description = "用户关注接口")
    public Response<?> follow(@RequestBody @Validated FollowUserReqVO followUserReqVO){
        return userRelationService.follow(followUserReqVO);
    }

    @PostMapping("/following/list")
    @ApiOperationLog(description = "查询用户关注列表")
    public PageResponse<FindFollowingListRspVO> findFollowingList(@RequestBody @Validated FindFollowingListReqVO findFollowingListReqVO){
        return userRelationService.findFollowingList(findFollowingListReqVO);
    }


}
