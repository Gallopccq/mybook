package com.mybook.mybook.user.biz.runner;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mybook.framework.common.util.JsonUtils;
import com.mybook.mybook.user.biz.constant.RedisKeyConstants;
import com.mybook.mybook.user.biz.domain.dataobject.PermissionDO;
import com.mybook.mybook.user.biz.domain.dataobject.RoleDO;
import com.mybook.mybook.user.biz.domain.dataobject.RolePermissionDO;
import com.mybook.mybook.user.biz.domain.mapper.PermissionDOMapper;
import com.mybook.mybook.user.biz.domain.mapper.RoleDOMapper;
import com.mybook.mybook.user.biz.domain.mapper.RolePermissionDOMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PushRolePermission2RedisRunner implements ApplicationRunner {

    @Resource
    RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RoleDOMapper roleDOMapper;
    @Resource
    private RolePermissionDOMapper rolePermissionDOMapper;
    @Resource
    private PermissionDOMapper permissionDOMapper;

    // 权限同步标记 key
    private static final String PUSH_PERMISSION_FLAG = "push.permission.flag";

    /**
     * 查询role，查询role-permission-rel，再查permission。（按需查permission）
     * @param args
     * @throws Exception
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("==> 服务启动，开始同步角色权限数据到 Redis 中...");

        try {
            boolean canPushed = redisTemplate.opsForValue().setIfAbsent(PUSH_PERMISSION_FLAG, 1, 60, TimeUnit.MINUTES);
            if (!canPushed) {
                // 已有服务抢占推送权限
                log.warn("==> 角色权限数据已经同步至 Redis 中，不再同步...");
                return;
            }

            List<RoleDO> roleDOS = roleDOMapper.selectEnabledList();
            if (CollUtil.isNotEmpty(roleDOS)) {
                // 获取所有角色ID
                List<Long> roleIds = roleDOS.stream().map(RoleDO::getId).toList();
                // 根据角色ID，查询对应权限
                List<RolePermissionDO> rolePermissionDOS = rolePermissionDOMapper.selectByRoleIds(roleIds);

                log.info(rolePermissionDOS.toString());

                if (Objects.isNull(rolePermissionDOS)) {
                    log.info("rolePermissionDOS is null: " + rolePermissionDOS);
                    return;
                }
                // 按角色ID 分组，每个ID 多个权限
                // todo: 有点复杂
                Map<Long, List<Long>> roleIdPermissionIdMap = rolePermissionDOS.stream().collect(
                        Collectors.groupingBy(RolePermissionDO::getRoleId,
                                Collectors.mapping(RolePermissionDO::getPermissionId, Collectors.toList()))
                );

                // 查询 app 端 所有被启用的权限
                List<PermissionDO> permissionDOS = permissionDOMapper.selectAppEnabledList();
                // 权限ID - 权限DO
                Map<Long, PermissionDO> permissionDOMap = permissionDOS.stream().collect(
                        Collectors.toMap(PermissionDO::getId, permissionDO -> permissionDO)
                );

                // 组织 角色ID - 权限 关系
                Map<String, List<String>> roleIdPermissionDOMap = Maps.newHashMap();

                roleDOS.forEach(roleDO -> {
                    Long roleId = roleDO.getId();
                    String roleKey = roleDO.getRoleKey();
                    List<Long> permissionIds = roleIdPermissionIdMap.get(roleId);
                    if (CollUtil.isNotEmpty(permissionIds)) {
                        List<String> permissionKeys = Lists.newArrayList(); // todo：为什么不用new而用这种方式？ 答，因为不用管泛型的声明
                        permissionIds.forEach(permissionId -> {
                            PermissionDO permissionDO = permissionDOMap.get(permissionId);
                            if (Objects.nonNull(permissionDO)) {
                                permissionKeys.add(permissionDO.getPermissionKey());
                            }
                        });
                        roleIdPermissionDOMap.put(roleKey, permissionKeys);
                    }
                });

                // 同步至redis，方便网关鉴权查询
                roleIdPermissionDOMap.forEach((roleKey, permissions) -> {
                    String key = RedisKeyConstants.buildRolePermissionsKey(roleKey);
                    redisTemplate.opsForValue().set(key, JsonUtils.toJsonString(permissions));
                });

            }
            log.info("==> 服务启动，成功同步角色权限数据到 Redis 中...");
        } catch (Exception e){
            log.error("==> 同步角色权限数据到 Redis 中失败: ", e);
        }


    }
}
