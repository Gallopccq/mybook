package com.mybook.framework.web.service.impl;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.mybook.common.core.domain.entity.SysUser;
import com.mybook.framework.web.service.SysPermissionService;

@Service // 这是service注入有什么用，是做什么的？
public class SysPermissionServiceImpl implements SysPermissionService {

    @Override
    public Set<String> getRolePermission(SysUser user) {
        Set<String> roles = new LinkedHashSet<>(); // 这是似乎是简化操作，实际应该如何操作?
        roles.add("admin");
        return roles;
    }

    @Override
    public Set<String> getMenuPermission(SysUser user) {
        Set<String> perms = new LinkedHashSet<>(); // 这是似乎是简化操作，实际应该如何操作?
        perms.add("system:user:list");
        perms.add("system:user:query");
        perms.add("system:menu:list");
        return perms;
    }
    
    
}
