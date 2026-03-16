package com.mybook.framework.web.service;

import com.mybook.common.core.domain.model.LoginBody;
import com.mybook.common.core.domain.model.LoginUser;

public interface SysLoginService {
    String login(LoginBody body);
    LoginUser getCurrentLoginUser();
}
