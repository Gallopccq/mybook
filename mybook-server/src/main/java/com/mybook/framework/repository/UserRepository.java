package com.mybook.framework.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.mybook.common.core.domain.entity.SysUser;

@Repository // 这个repository做什么的？
public class UserRepository {
    private final JdbcTemplate jdbcTemplate; // 这个JdbcTemplate干什么的？
    
    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<SysUser> findByUsername(String username) {
        String sql = """
            select user_id, user_name, password, nick_name, status
            from sys_user where user_name = ?
        """;
        List<SysUser> list = jdbcTemplate.query(sql, (rs, i) -> {
            SysUser u = new SysUser();
            u.setUserId(rs.getLong("user_id"));
            u.setUserName(rs.getString("user_name"));
            u.setPassword(rs.getString("password"));
            u.setNickName(rs.getString("nick_name"));
            u.setStatus(rs.getString("status"));
            return u;
        }, username);
        return list.stream().findFirst(); // 这个stream().findFirst()是什么？
    }
}
