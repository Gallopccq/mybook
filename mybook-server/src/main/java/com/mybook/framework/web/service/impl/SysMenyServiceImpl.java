package com.mybook.framework.web.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.mybook.common.core.domain.entity.SysMenu;
import com.mybook.system.service.ISysMenuService;

@Service
public class SysMenyServiceImpl implements ISysMenuService{

    @Override
    public List<SysMenu> selectMenuTreeByUserId(Long userId) {
        List<SysMenu> list = new ArrayList<>(); // 这是似乎是简化操作，实际应该如何操作?
        list.add(new SysMenu(1L, "工作台", "/dashboard"));
        list.add(new SysMenu(2L, "个人忠心", "/profile"));
        return list;
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
