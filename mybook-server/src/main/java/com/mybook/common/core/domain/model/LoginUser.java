package com.mybook.common.core.domain.model;

import com.mybook.common.core.domain.entity.SysUser;

public class LoginUser {
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public SysUser getUser() {
        return user;
    }

    public void setUser(SysUser user) {
        this.user = user;
    }

    private SysUser user;
}
