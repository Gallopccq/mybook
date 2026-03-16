package com.mybook.server.controller.system;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mybook.common.constant.Constants;
import com.mybook.common.core.domain.AjaxResult;
import com.mybook.common.core.domain.entity.SysMenu;
import com.mybook.common.core.domain.entity.SysUser;
import com.mybook.common.core.domain.model.LoginBody;
import com.mybook.common.core.domain.model.LoginUser;
import com.mybook.framework.web.annotation.NoAuth;
import com.mybook.framework.web.service.SysLoginService;
import com.mybook.framework.web.service.SysPermissionService;
import com.mybook.system.service.ISysMenuService;

/**
 * 登陆验证
 * 
 * @Author mybook
 */
@RestController
public class SysLoginController {

    private final Logger logger = LoggerFactory.getLogger(SysLoginController.class);

    private final SysLoginService loginService;
    private final ISysMenuService menuService;
    private final SysPermissionService permissionService;

    public SysLoginController(SysLoginService loginService,
                              ISysMenuService menuService,
                              SysPermissionService permissionService) {
        this.loginService = loginService;
        this.menuService = menuService;
        this.permissionService = permissionService;
    }


    /**
     * 登陆方法
     *
     * @Param loginBody 登陆信息
     * @return 结果
     */
    @NoAuth // 为什么要加免登录注解？
    @PostMapping("/login")
    public AjaxResult login(@RequestBody LoginBody loginBody) {
        AjaxResult ajax = AjaxResult.success();
        String token = loginService.login(loginBody);
        ajax.put(Constants.TOKEN, token);
        logger.debug("用户登录成功：" + loginBody.getUsername());
        return ajax;
    }
    
    @PostMapping("/logout")
    public AjaxResult logout() {
        loginService.logout();
        return AjaxResult.success("退出成功");
    }

    @GetMapping("/getInfo")
    public AjaxResult getInfo() {
        LoginUser loginUser = loginService.getCurrentLoginUser();
        SysUser user = loginUser.getUser(); // loginuser和user有什么区别？
        Set<String> roles = permissionService.getRolePermission(user); // roles用来做什么？
        Set<String> permissions = permissionService.getMenuPermission(user); // permission有什么东西？
        AjaxResult ajax = AjaxResult.success();
        ajax.put("user", user);
        ajax.put("roles", roles);
        ajax.put("permissions", permissions);
        return ajax;
    }

    @GetMapping("/getRouters")
    public AjaxResult getRouters() {
        Long userId = loginService.getCurrentLoginUser().getUser().getUserId();
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
        return AjaxResult.success(menuService.buildMenus(menus));
    }
}
