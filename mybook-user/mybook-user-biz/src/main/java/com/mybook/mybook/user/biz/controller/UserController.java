package com.mybook.mybook.user.biz.controller;

import com.mybook.framework.biz.operationlog.aspect.ApiOperationLog;
import com.mybook.framework.common.response.Response;
import com.mybook.mybook.user.biz.model.vo.UpdateUserInfoReqVO;
import com.mybook.mybook.user.biz.service.UserService;
import com.mybook.mybook.user.dto.req.FindUserByIdReqDTO;
import com.mybook.mybook.user.dto.req.FindUserByPhoneReqDTO;
import com.mybook.mybook.user.dto.req.RegisterUserReqDTO;
import com.mybook.mybook.user.dto.req.UpdateUserPasswordReqDTO;
import com.mybook.mybook.user.dto.resp.FindUserByIdRspDTO;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


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

    @PostMapping(value = "/password/update")
    @ApiOperationLog(description = "密码更新")
    public Response<?> updatePassword(@RequestBody @Validated UpdateUserPasswordReqDTO updateUserPasswordReqDTO){
        return userService.updatePassword(updateUserPasswordReqDTO);
    }

    /**
     * 内部调用：mybook-auth
     * @param registerUserReqDTO
     * @return
     */
    @PostMapping(value = "/register")
    @ApiOperationLog(description = "用户注册")
    public Response<?> register(@RequestBody @Validated RegisterUserReqDTO registerUserReqDTO){
        return userService.register(registerUserReqDTO);
    }

    @PostMapping(value = "/findByPhone")
    @ApiOperationLog(description = "手机号查询用户信息")
    public Response<?> fingByPhone(@RequestBody @Validated FindUserByPhoneReqDTO findUserByPhoneReqDTO){
        return userService.findByPhone(findUserByPhoneReqDTO);
    }

    @PostMapping("/findById")
    @ApiOperationLog(description = "查询用户信息")
    public Response<FindUserByIdRspDTO> findById(@RequestBody @Validated FindUserByIdReqDTO findUserByIdReqDTO) {
        return userService.findById(findUserByIdReqDTO);
    }

    @PostMapping("/findByIdWithDatabase")
    @ApiOperationLog(description = "查询用户信息")
    public Response<FindUserByIdRspDTO> findByIdWithDatabase(@RequestBody @Validated FindUserByIdReqDTO findUserByIdReqDTO) {
        return userService.findByIdWithDatabase(findUserByIdReqDTO);
    }

    @PostMapping("/findByIdWithRedis")
    @ApiOperationLog(description = "查询用户信息")
    public Response<FindUserByIdRspDTO> findByIdWithRedis(@RequestBody @Validated FindUserByIdReqDTO findUserByIdReqDTO) {
        return userService.findByIdWithRedis(findUserByIdReqDTO);
    }
}
