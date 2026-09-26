package com.mybook.mybook.user.relation.biz.model.vo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class FindFansUserRspVO {

    /**
     * 返回参数（单体）： 用户昵称，头像，粉丝数，笔记数，用户 id
     */

    private Long userId;
    private String nickName;
    private String avatar;
    private long followerTotal;
    private long noteTotal;
    private String introduction;
}
