package com.mybook.mybook.user.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UpdateUserPasswordReqDTO {

    @NotBlank(message = "密码不能为空")
    private String encodePassword;
}
