package com.ant.little.core.dao;

import com.ant.little.core.domain.MemberConfigDO;
import com.ant.little.core.domain.MemberConfigDOExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface MemberConfigDOMapper {
    long countByExample(MemberConfigDOExample example);

    int deleteByExample(MemberConfigDOExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MemberConfigDO row);

    int insertSelective(MemberConfigDO row);

    List<MemberConfigDO> selectByExample(MemberConfigDOExample example);

    MemberConfigDO selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") MemberConfigDO row, @Param("example") MemberConfigDOExample example);

    int updateByExample(@Param("row") MemberConfigDO row, @Param("example") MemberConfigDOExample example);

    int updateByPrimaryKeySelective(MemberConfigDO row);

    int updateByPrimaryKey(MemberConfigDO row);
}