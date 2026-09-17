package com.mybook.mybook.user.relation.biz.domain.mapper;

import com.mybook.mybook.user.relation.biz.domain.dataobject.FollowingDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FollowingDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(FollowingDO record);

    int insertSelective(FollowingDO record);

    FollowingDO selectByPrimaryKey(Long id);

    List<FollowingDO> selectByUserId(Long id);

    int updateByPrimaryKeySelective(FollowingDO record);

    int updateByPrimaryKey(FollowingDO record);

    int selectByUserIdAndFollowingId(@Param("userId") Long userId, @Param("followingUserId") Long followingUserId);

    int deleteByUserIdAndFollowingId(@Param("userId") Long userId, @Param("followingUserId") Long followingUserId);

    long selectCountByUserId(long userId);

    List<FollowingDO> selectPageListByUserId(@Param("userId") Long userId, @Param("offset") long offset, @Param("limit") long limit);

    List<FollowingDO> selectByUserIdWithLimit(@Param("userId") Long userId);
}