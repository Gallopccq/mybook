package com.mybook.mybook.user.relation.biz.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class FindFollowingListRspVO {
    private Long userId;
    private String avatar;
    private String nickName;
    private String introduction;

}
