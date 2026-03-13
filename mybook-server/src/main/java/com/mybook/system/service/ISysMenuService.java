package com.mybook.system.service;

import java.util.List;
import java.util.Map;

import com.mybook.common.core.domain.entity.SysMenu;

public interface ISysMenuService {
    List<SysMenu> selectMenyTreeByUserId(Long userId);
    List<Map<String, Object>> buildMenus(List<SysMenu> menus);
}
