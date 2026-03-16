package com.mybook.framework.web.service.impl;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.mybook.common.core.domain.entity.SysUser;
import com.mybook.framework.repository.AuthRepository;
import com.mybook.framework.web.service.SysPermissionService;

@Service // 这是service注入有什么用，是做什么的？
public class SysPermissionServiceImpl implements SysPermissionService {

    private final AuthRepository authRepository;

    public SysPermissionServiceImpl(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    @Override
    public Set<String> getRolePermission(SysUser user) {
        return authRepository.findRoleKeysByUserId(user.getUserId());
    }

    @Override
    public Set<String> getMenuPermission(SysUser user) {
        return authRepository.findPermsByUserId(user.getUserId());
    }
    
    
}
