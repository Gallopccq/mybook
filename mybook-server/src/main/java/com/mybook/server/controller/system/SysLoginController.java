package com.mybook.server.controller.system;

import com.mybook.common.constant.Constants;
import com.mybook.common.core.domain.AjaxResult;
import com.mybook.common.core.domain.entity.SysMenu;
import com.mybook.common.core.domain.entity.SysUser;
import com.mybook.common.core.domain.model.LoginBody;
import com.mybook.common.core.domain.model.LoginUser;
import com.mybook.framework.web.service.SysLoginService;
import com.mybook.framework.web.service.SysPermissionService;
import com.mybook.system.service.ISysMenuService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * 登陆验证
 * 
 * @Author mybook
 */
@RestController
public class SysLoginController {

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
    @PostMapping("/login")
    public AjaxResult login(@RequestBody LoginBody loginBody) {
        AjaxResult ajax = AjaxResult.success();
        String token = loginService.login(loginBody);
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }

    @GetMapping("/getInfo")
    public AjaxResult getInfo() {
        LoginUser loginUser = loginService.getCurrentLoginUser();
        SysUser user = loginUser.getUser();
        Set<String> roles = permissionService.getRolePermission(user);
        Set<String> permissions = permissionService.getMenuPermission(user);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("user", user);
        ajax.put("roles", roles);
        ajax.put("permissions", permissions);
        return ajax;
    }

    @GetMapping("/getRouters")
    public AjaxResult getRouters() {
        Long userId = sloginService.getCurrentLoginUser().getUser().getUserId();
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
        return AjaxResult.success(menuService.buildMenus(menus));
    }
}
