package com.mybook.mybook.user.biz.domain.mapper;

import com.mybook.mybook.user.biz.domain.dataobject.RolePermissionDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RolePermissionDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(RolePermissionDO record);

    int insertSelective(RolePermissionDO record);

    RolePermissionDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RolePermissionDO record);

    int updateByPrimaryKey(RolePermissionDO record);

    // 此处需要用@Param("roleIds"),使mapper.xml能通过#{roleIds}访问数据，否则变量必须是javabean。
    // todo: 什么是javabean
    List<RolePermissionDO> selectByRoleIds(@Param("roleIds") List<Long> roleIds);
}