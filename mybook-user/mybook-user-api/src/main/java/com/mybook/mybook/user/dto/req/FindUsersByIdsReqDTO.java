package com.mybook.mybook.user.dto.req;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class FindUsersByIdsReqDTO {
    @NotNull(message = "用户 ID 集合不能为空")
    @Size(min = 1, max = 10, message = "用户 ID 集合大小必须大于等于 1, 小于等于 10")
    // 考虑到查询性能，避免调用方一次的批量查询的太多，这里限制 ID 集合的大小最大为 10， 符合分页查询的习惯。
    private List<Long> ids;
}
