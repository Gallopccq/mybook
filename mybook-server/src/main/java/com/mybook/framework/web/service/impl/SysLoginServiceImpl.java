package com.mybook.framework.web.service.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mybook.common.constant.Constants;
import com.mybook.common.core.domain.model.LoginBody;
import com.mybook.common.core.domain.model.LoginUser;
import com.mybook.framework.repository.AuthRepository;
import com.mybook.framework.repository.UserRepository;
import com.mybook.framework.web.service.SysLoginService;
import com.mybook.framework.web.service.TokenService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 	本实现（混合模式）:
 * 有状态;	用户信息存储在Redis中; 会话管理, 可主动失效（删除Redis键）; 扩展性​依赖Redis集群
 * 
 */
@Service
public class SysLoginServiceImpl implements SysLoginService{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HttpServletRequest request;
    private final ConcurrentMap<String, LoginUser> sessions = new ConcurrentHashMap<>();
    // 似乎session的信息就是用一个Map来存储，是否合适？这个ConcurrentMap是否只是保证并发存取时的一致性？
    /**
     *  单机、小型或演示性质的应用中，使用内存中的 ConcurrentHashMap来存储会话信息是一种简单直接的实现方式
     * 但是，对于生产环境，尤其是需要分布式部署（多实例）的应用，需要替换为Redis等集中式存储。
     * ConcurrentHashMap 是线程安全的哈希表实现，保证线程安全性和数据一致性
    */
    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public SysLoginServiceImpl(AuthRepository authRepository, PasswordEncoder passwordEncoder, HttpServletRequest request, TokenService tokenService, UserRepository userRepository) {
        this.authRepository = authRepository;
        this.passwordEncoder = passwordEncoder;
        this.request = request;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @Override
    public String login(LoginBody body) {
        if (body == null || body.getUsername() == null || body.getPassword() == null){
            throw new IllegalArgumentException("用户名或密码不能为空");
        }
        var user = userRepository.findByUsername(body.getUsername()) // 这个var干什么的？
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        if (!"0".equals(user.getStatus())) {
            throw new IllegalStateException("用户已停用");
        }
        if (!passwordEncoder.matches(body.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("密码错误");
        }

        LoginUser loginUser = new LoginUser();
        loginUser.setUser(user);
        loginUser.setRoles(authRepository.findRoleKeysByUserId(user.getUserId()));
        loginUser.setPermissions(authRepository.findPermsByUserId(user.getUserId()));
        return tokenService.createToken(loginUser);
    }

    @Override
    public LoginUser getCurrentLoginUser() {
        String auth = request.getHeader(Constants.AUTH_HEADER);
        if (auth == null || auth.isBlank()){
            throw new IllegalStateException("缺少 Authorization 请求头");
        } // 这个请求头是什么？后面一般跟什么信息？
        auth = auth.trim();
        String jwt = auth.startsWith(Constants.BEARER_PREFIX)
                ? auth.substring(Constants.BEARER_PREFIX.length())
                : auth;
        jwt = jwt.trim();
        logger.info("当前用户 jwt: " + jwt);
                // 啥意思？BEARER是什么？BEARER后面有什么？
        /**
         * “持有此令牌（Bearer Token）的任何一方”都被授权访问相关资源。它是一种最常见的Token使用方式。
         * 定义在RFC 6750标准中
         * Bearer关键字后面，紧跟一个空格，然后就是具体的令牌字符串。
         * 例如：Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...。
         */
        return tokenService.parseLoginUser(jwt)
                .orElseThrow(() -> new IllegalStateException("登录已过期"));

    }
    
}


// 这个token与cookie有什么区别？
// token为什么不能设置为永久？
/**
 * token 用于jwt的无状态访问，cokkie用于浏览器保存的有状态访问。（无状态：服务器不保存，用户保存信息；有状态：服务器保存用户信息，分布式情况需要一致性保证）
 * jwt用于跨域访问，session用与状态保存。
 * token设置为永久会导致用户信息被盗用，高权限用户永久操作。
 */