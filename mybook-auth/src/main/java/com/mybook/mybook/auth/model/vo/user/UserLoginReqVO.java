package com.mybook.mybook.auth.model.vo.user;

import com.mybook.framework.common.validator.PhoneNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * 入参
 * {
 *     "phone": "18011119108", // 手机号
 *     "code": "218603", // 登录验证码，验证码登录时，需要填写
 *     "password": "xx", // 密码登录时，需要填写
 *     "type": 1 // 登录类型，1表示手机号验证码登录；2表示账号密码登录
 * }
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class UserLoginReqVO {

    @NotBlank(message = "手机号不能为空")
    @PhoneNumber
    private String phone;

    private String code;

    private String password;

    @NotNull(message = "登录类型不能为空")
    private Integer type;
}
