package com.ant.little.core.dao;

import com.ant.little.core.domain.RequestCounterDO;
import com.ant.little.core.domain.RequestCounterDOExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface RequestCounterDOMapper {
    long countByExample(RequestCounterDOExample example);

    int deleteByExample(RequestCounterDOExample example);

    int deleteByPrimaryKey(Long id);

    int insert(RequestCounterDO row);

    int insertSelective(RequestCounterDO row);

    List<RequestCounterDO> selectByExample(RequestCounterDOExample example);

    RequestCounterDO selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") RequestCounterDO row, @Param("example") RequestCounterDOExample example);

    int updateByExample(@Param("row") RequestCounterDO row, @Param("example") RequestCounterDOExample example);

    int updateByPrimaryKeySelective(RequestCounterDO row);

    int updateByPrimaryKey(RequestCounterDO row);
}