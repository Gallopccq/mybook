package com.mybook.framework.web.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mybook.common.core.domain.entity.SysMenu;
import com.mybook.framework.repository.AuthRepository;
import com.mybook.system.service.ISysMenuService;

@Service
public class SysMenuServiceImpl implements ISysMenuService{

    private final AuthRepository authRepository;

    public SysMenuServiceImpl(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    @Override
    public List<SysMenu> selectMenuTreeByUserId(Long userId) {
        List<SysMenu> all = authRepository.findMenusByUserId(userId);
        Map<Long, SysMenu> map = all.stream().collect(Collectors.toMap(SysMenu::getMenuId, m -> m));
        // 这个Collectors.toMap(SysMenu::getMenuId, m -> m)做了什么？
        List<SysMenu> roots = new ArrayList<>();

        for (SysMenu m : all){
            if (m.getParentId() == 0){
                roots.add(m);
            } else {
                SysMenu parent = map.get(m.getParentId());
                if (parent != null){
                    parent.getChildren().add(m);
                }
            }
        }
        return roots; // 这个roots是什么东西？
    }

    @Override
    public List<Map<String, Object>> buildMenus(List<SysMenu> menus) {
        List<Map<String, Object>> routers = new ArrayList<>();
        for (SysMenu m : menus) { // 这里做了什么工作？
            Map<String ,Object> r = new LinkedHashMap<>(); // 为什么用LinkedHashMap？
            r.put("name", m.getMenuName());
            r.put("path", m.getPath());
            r.put("children", buildMenus(m.getChildren()));
            routers.add(r);
        }
        return routers;
    }
    
}
