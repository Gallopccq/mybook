package com.mybook.common.core.domain.entity;

import java.util.ArrayList;
import java.util.List;

public class SysMenu {
    private Long menuId;
    private Long parentId;
    private String menuName;
    private String path;
    private String perms;
    private Integer orderNum;
    private List<SysMenu> children = new ArrayList<>();

    public SysMenu(){}

    public SysMenu(Long menyId, String menuName, String path) {
        this.menuId = menyId;
        this.menuName = menuName;
        this.path = path;
    }

    public Long getMenuId() {
        return menuId;
    }

    public void setMenuId(Long menyId) {
        this.menuId = menyId;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<SysMenu> getChildren() {
        return children;
    }

    public void setChildren(List<SysMenu> children) {
        this.children = children;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getPerms() {
        return perms;
    }

    public void setPerms(String perms) {
        this.perms = perms;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }
}
