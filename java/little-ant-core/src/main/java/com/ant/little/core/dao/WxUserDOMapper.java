package com.ant.little.core.dao;

import com.ant.little.core.domain.WxUserDO;
import com.ant.little.core.domain.WxUserDOExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface WxUserDOMapper {
    long countByExample(WxUserDOExample example);

    int deleteByExample(WxUserDOExample example);

    int deleteByPrimaryKey(Long id);

    int insert(WxUserDO row);

    int insertSelective(WxUserDO row);

    List<WxUserDO> selectByExample(WxUserDOExample example);

    WxUserDO selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") WxUserDO row, @Param("example") WxUserDOExample example);

    int updateByExample(@Param("row") WxUserDO row, @Param("example") WxUserDOExample example);

    int updateByPrimaryKeySelective(WxUserDO row);

    int updateByPrimaryKey(WxUserDO row);
}