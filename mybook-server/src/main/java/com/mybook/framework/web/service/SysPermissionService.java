package com.mybook.framework.web.service;

import java.util.Set;

import com.mybook.common.core.domain.entity.SysUser;

public interface SysPermissionService {
    Set<String> getRolePermission(SysUser user);
    Set<String> getMenuPermission(SysUser user);

}
