package com.mybook.mybook.user.dto.req;

import com.mybook.framework.common.validator.PhoneNumber;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RegisterUserReqDTO {

    @NotBlank(message = "手机号不能为空")
    @PhoneNumber
    private String phone;
}
