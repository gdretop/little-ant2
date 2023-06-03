package com.ant.little.core.dao;

import com.ant.little.core.domain.Counter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface CountersMapper {

    Counter getCounter(@Param("id") Integer id);

    void upsertCount(Counter counter);

    void clearCount(@Param("id") Integer id);
}
