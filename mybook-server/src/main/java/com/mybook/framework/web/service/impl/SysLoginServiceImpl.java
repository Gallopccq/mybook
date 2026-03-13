package com.mybook.framework.web.service.impl;

import org.springframework.stereotype.Service;

import com.mybook.common.core.domain.model.LoginBody;
import com.mybook.common.core.domain.model.LoginUser;
import com.mybook.framework.web.service.SysLoginService;

@Service
public class SysLoginServiceImpl implements SysLoginService{

    @Override
    public String login(LoginBody body) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public LoginUser getCurrentLoginUser() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
