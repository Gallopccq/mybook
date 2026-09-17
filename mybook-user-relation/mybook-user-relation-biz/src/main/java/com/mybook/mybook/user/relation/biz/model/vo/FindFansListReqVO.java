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
public class FindFansListReqVO {
    /**
     * 请求参数：userId
     */
    @NotNull(message = "用户 ID 不能为空")
    private Long id;

    /**
     * 请求页码：默认为 1
     */
    private Integer pageNo = 1;
}
