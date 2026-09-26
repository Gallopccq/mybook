package com.mybook.mybook.user.relation.biz.domain.mapper;

import com.mybook.mybook.user.relation.biz.domain.dataobject.FansDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FansDOMapper {
    int deleteByPrimaryKey(Long id);

    int deleteByUserIdAndFansUserId(@Param("userId") Long userId, @Param("fansUserId") Long fansUserId);

    int insert(FansDO record);

    int insertSelective(FansDO record);

    FansDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FansDO record);

    int updateByPrimaryKey(FansDO record);

    Long selectCountByUserId(Long userId);

    List<FansDO> selectByUserId(Long userId);

    List<FansDO> selectPageListByUserId(@Param("userId") Long userId, @Param("offset") Integer offset, @Param("limit") Long limit);
}