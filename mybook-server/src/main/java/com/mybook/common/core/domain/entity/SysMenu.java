package com.mybook.common.core.domain.entity;

import java.util.ArrayList;
import java.util.List;

public class SysMenu {
    private Long menyId;
    private String menuName;
    private String path;
    private List<SysMenu> children = new ArrayList<>();

    public SysMenu(Long menyId, String menuName, String path) {
        this.menyId = menyId;
        this.menuName = menuName;
        this.path = path;
    }

    public Long getMenyId() {
        return menyId;
    }

    public void setMenyId(Long menyId) {
        this.menyId = menyId;
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
}
