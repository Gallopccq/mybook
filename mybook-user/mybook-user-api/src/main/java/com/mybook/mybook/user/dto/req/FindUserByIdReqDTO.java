package com.mybook.mybook.user.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class FindUserByIdReqDTO {
    @NotNull(message = "用户 ID 不能为空")
    private Long id;
}
