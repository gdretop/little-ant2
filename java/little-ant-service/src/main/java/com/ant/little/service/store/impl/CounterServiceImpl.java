package com.ant.little.service.store.impl;

import com.ant.little.core.dao.CountersMapper;
import com.ant.little.core.domain.Counter;
import com.ant.little.service.store.CounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CounterServiceImpl implements CounterService {

    final CountersMapper countersMapper;

    public CounterServiceImpl(@Autowired CountersMapper countersMapper) {
        this.countersMapper = countersMapper;
    }

    @Override
    public Optional<Counter> getCounter(Integer id) {
        return Optional.ofNullable(countersMapper.getCounter(id));
    }

    @Override
    public void upsertCount(Counter counter) {
        countersMapper.upsertCount(counter);
    }

    @Override
    public void clearCount(Integer id) {
        countersMapper.clearCount(id);
    }
}
