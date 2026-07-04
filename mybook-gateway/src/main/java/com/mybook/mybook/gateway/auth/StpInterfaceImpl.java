package com.mybook.mybook.gateway.auth;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.collection.CollUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.mybook.mybook.gateway.constant.RedisKeyConstants;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class StpInterfaceImpl implements StpInterface {
    @Resource
    RedisTemplate<String, String> redisTemplate;
    @Resource
    ObjectMapper objectMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 返回 loginId 拥有的权限列表
        // todo 从 redis 中获取
        List<String> roles = getRoleList(loginId, loginType);

        if (CollUtil.isNotEmpty(roles)){
            List<String> rolePermissionKeys = roles.stream().map(RedisKeyConstants::buildRolePermissionsKey).toList();
            log.info(rolePermissionKeys.toString());
            List<String> rolePermissionValues = redisTemplate.opsForValue().multiGet(rolePermissionKeys);
            log.info(rolePermissionValues.toString());
            if (CollUtil.isNotEmpty(rolePermissionValues)) {
                List<String> permissions = Lists.newArrayList();
                rolePermissionValues.forEach(p -> {
//                    if (p == null) return;
                    List<String> permission = parseJsonString(p);
//                    log.info(permission.toString());
                    if (permission != null) {
                        permissions.addAll(permission);
                    }
                });
                log.info("## 获取用户权限列表, loginId: {}, permissions: {}", loginId, permissions);
                return permissions;
            }
        }
        return null;
    }

//    @SneakyThrows  // 改为RuntimeException
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 返回 loginId 拥有的角色列表
        // 从 redis 中获取

        String userRoleKey = RedisKeyConstants.buildUserRoleKey(Long.valueOf(loginId.toString()));
        String roles = redisTemplate.opsForValue().get(userRoleKey);
        log.info("## 获取用户角色列表, userRoleKey: {}, roles: {}", userRoleKey, roles);
        if (StringUtils.isBlank(roles)){
            return null;
        }
        return parseJsonString(roles);
    }
    
    public List<String> parseJsonString(String roles) {
        if (roles == null) return null;
        try {
            return objectMapper.readValue(roles, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            log.error("Json 解析错误:", e);
            return null;
        }

    }
}
