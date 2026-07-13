package com.mybook.mybook.user.biz.controller;

import com.mybook.framework.common.response.Response;
import com.mybook.mybook.user.biz.model.vo.UpdateUserInfoReqVO;
import com.mybook.mybook.user.biz.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
//@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;


    // 请勿添加切面日志注解 @ApiOperationLog，此接口包含文件流上传，Jackson 序列化会有问题！！！
    @PostMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<?> updateUserInfo(@Validated UpdateUserInfoReqVO updateUserInfoReqVO){
        return userService.updateUserInfo(updateUserInfoReqVO);
    }
}
