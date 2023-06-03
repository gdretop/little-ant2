package com.ant.little.core.dao;

import com.ant.little.core.domain.RequestLogDO;
import com.ant.little.core.domain.RequestLogDOExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface RequestLogDOMapper {
    long countByExample(RequestLogDOExample example);

    int deleteByExample(RequestLogDOExample example);

    int deleteByPrimaryKey(Long id);

    int insert(RequestLogDO row);

    int insertSelective(RequestLogDO row);

    List<RequestLogDO> selectByExample(RequestLogDOExample example);

    RequestLogDO selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") RequestLogDO row, @Param("example") RequestLogDOExample example);

    int updateByExample(@Param("row") RequestLogDO row, @Param("example") RequestLogDOExample example);

    int updateByPrimaryKeySelective(RequestLogDO row);

    int updateByPrimaryKey(RequestLogDO row);
}