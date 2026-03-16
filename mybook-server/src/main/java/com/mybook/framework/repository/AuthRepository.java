package com.mybook.framework.repository;

import java.util.List;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.mybook.common.core.domain.entity.SysMenu;

@Repository
public class AuthRepository {
    private final JdbcTemplate jdbcTemplate;
    /**
     * JdbcTemplate适用场景​
        1. 喜欢“直接写SQL”的开发模式。
        2. 遗留系统改造，或与 Spring 深度集成的小型项目。
        3. 需要执行复杂、非标准或高度优化的 SQL。
        4. 微服务中简单的数据访问逻辑。
     */

    public AuthRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public Set<String> findRoleKeysByUserId(Long userId) {
        String sql = """
            select r.role_key
            from sys_role r
            join sys_user_role ur on ur.role_id = r.role_id
            where ur.user_id = ? and r.status = '0'
        """;
        return Set.copyOf(jdbcTemplate.queryForList(sql, String.class, userId));
        // 这个jdbcTemplate.queryForList(sql, String.class, userId)是干什么的？
    }

    public Set<String> findPermsByUserId(Long userId){
        String sql = """
            select distinct m.perms
            from sys_menu m
            join sys_role_menu rm on rm.menu_id = m.menu_id
            join sys_user_role ur on ur.role_id = rm.role_id
            where ur.user_id = ? and m.status = '0' and m.perms is not null and m.perms <> ''
        """;
        return Set.copyOf(jdbcTemplate.queryForList(sql, String.class, userId));
    }

    public List<SysMenu> findMenusByUserId(Long userId) {
        String sql = """
            select distinct m.menu_id, m.parent_id, m.menu_name, m.path, m.perms, m.order_num
            from sys_menu m
            join sys_role_menu rm on rm.menu_id = m.menu_id
            join sys_user_role ur on ur.role_id = rm.role_id
            where ur.user_id = ? and m.status = '0'
            order by m.parent_id, m.order_num, m.menu_id
        """;
        return jdbcTemplate.query(sql, (rs, i) -> {
            SysMenu m = new SysMenu();
            m.setMenuId(rs.getLong("menu_id"));
            m.setParentId(rs.getLong("parent_id"));
            m.setMenuName(rs.getString("menu_name"));
            m.setPath(rs.getString("path"));
            m.setPerms(rs.getString("perms"));
            m.setOrderNum(rs.getInt("order_num"));
            return m;
        }, userId);
    }
}
