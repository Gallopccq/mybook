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
public class FollowUserReqVO {

    @NotNull(message = "被关注的用户 ID 不能为空")
    private Long id;
}
