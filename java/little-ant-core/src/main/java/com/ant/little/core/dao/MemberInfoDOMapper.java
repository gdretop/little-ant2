package com.ant.little.core.dao;

import com.ant.little.core.domain.MemberInfoDO;
import com.ant.little.core.domain.MemberInfoDOExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface MemberInfoDOMapper {
    long countByExample(MemberInfoDOExample example);

    int deleteByExample(MemberInfoDOExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MemberInfoDO row);

    int insertSelective(MemberInfoDO row);

    List<MemberInfoDO> selectByExample(MemberInfoDOExample example);

    MemberInfoDO selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") MemberInfoDO row, @Param("example") MemberInfoDOExample example);

    int updateByExample(@Param("row") MemberInfoDO row, @Param("example") MemberInfoDOExample example);

    int updateByPrimaryKeySelective(MemberInfoDO row);

    int updateByPrimaryKey(MemberInfoDO row);
}