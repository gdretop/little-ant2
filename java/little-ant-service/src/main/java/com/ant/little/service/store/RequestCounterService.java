package com.ant.little.service.store;

import com.alibaba.fastjson.JSON;
import com.ant.little.common.model.CommonCacheObj;
import com.ant.little.common.model.Response;
import com.ant.little.core.config.EnvConfig;
import com.ant.little.core.dao.RequestCounterDOMapper;
import com.ant.little.core.domain.RequestCounterDO;
import com.ant.little.core.domain.RequestCounterDOExample;
import com.ant.little.model.dto.RequestCounterDTO;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author: little-ant
 * 描述:
 * @date: 2023/2/26
 * @Version 1.0
 **/
@Service
public class RequestCounterService {
    private final static Logger logger = LoggerFactory.getLogger(RequestCounterService.class);
    @Resource
    private RequestCounterDOMapper requestCounterDOMapper;
    @Autowired
    private EnvConfig envConfig;

    private Cache<String, CommonCacheObj<RequestCounterDTO>> localCache = CacheBuilder.newBuilder()
            .concurrencyLevel(4)
            .initialCapacity(100)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(5000)
            .build();

    public void invalidateAll() {
        localCache.invalidateAll();
    }

    /**
     * 插入一条计数控制器
     *
     * @param requestCounterDTO
     * @return
     */
    public Response<RequestCounterDTO> insert(RequestCounterDTO requestCounterDTO) {
        String env = envConfig.getCurEnv();
        requestCounterDTO.setEnv(env);
        RequestCounterDO requestCounterDO = dto2DO(requestCounterDTO);
        int effect = 0;
        try {
            effect = requestCounterDOMapper.insertSelective(requestCounterDO);
        } catch (Exception e) {
            logger.error("插入数据失败 {}", e.toString(), e);
            return Response.newFailure("插入数据失败", "");
        }
        if (effect == 1) {
            String key = genKey(requestCounterDTO);
            localCache.invalidate(key);
            requestCounterDTO = query(requestCounterDTO);
            return Response.newSuccess(requestCounterDTO);
        } else {
            logger.error("插入信息失败 {}", JSON.toJSONString(requestCounterDTO));
            return Response.newFailure("写入失败", "");
        }
    }

    public int updateById(RequestCounterDTO requestCounterDTO) {
        RequestCounterDO requestCounterDO = dto2DO(requestCounterDTO);
        return requestCounterDOMapper.updateByPrimaryKeySelective(requestCounterDO);
    }

    /**
     * 增加一次请求记录
     *
     * @param requestCounterDTO
     * @return
     */
    public int addCount(RequestCounterDTO requestCounterDTO) {
        RequestCounterDTO counter = query(requestCounterDTO);
        RequestCounterDO counterDO = new RequestCounterDO();
        counterDO.setId(counter.getId());
        counterDO.setRequestNum(counter.getRequestNum() + 1);
        counter.setRequestNum(counter.getRequestNum() + 1);
        requestCounterDOMapper.updateByPrimaryKeySelective(counterDO);
        return counter.getRequestNum();
    }


    public RequestCounterDTO query(RequestCounterDTO requestCounterDTO) {
        String env = envConfig.getCurEnv();
        requestCounterDTO.setEnv(env);
        //判断是否有缓存
        String key = genKey(requestCounterDTO);
        CommonCacheObj<RequestCounterDTO> commonCacheObj = localCache.getIfPresent(key);
        if (commonCacheObj != null && commonCacheObj.data != null) {
            return commonCacheObj.data;
        }
        //无缓存读取db信息
        RequestCounterDTO record = queryRecord(requestCounterDTO);
        if (record != null) {
            localCache.put(key, new CommonCacheObj(record));
            return record;
        }
        return null;
    }

    /**
     * 删除一个用户当天的所有计数器
     *
     * @param appId
     * @param openId
     * @param bizDate
     */
    public void deleteUserCounter(String appId, String openId, String bizDate) {
        RequestCounterDOExample example = new RequestCounterDOExample();
        example.createCriteria().andEnvEqualTo(envConfig.getCurEnv()).andAppidEqualTo(appId)
                .andOpenIdEqualTo(openId).andBizDateEqualTo(bizDate);
        List<RequestCounterDO> result = requestCounterDOMapper.selectByExample(example);
        for (RequestCounterDO item : result) {
            RequestCounterDTO dto = do2Dto(item);
            requestCounterDOMapper.deleteByPrimaryKey(dto.getId());
            String key = genKey(dto);
            localCache.invalidate(key);
        }
    }

    private String genKey(RequestCounterDTO requestCounterDTO) {
        String key = String.format("%s-%s-%s-%s-%s-%s", requestCounterDTO.getEnv(), requestCounterDTO.getAppid(),
                requestCounterDTO.getOpenId(), requestCounterDTO.getType(), requestCounterDTO.getRequestKey(),
                requestCounterDTO.getBizDate());
        return key;
    }

    private RequestCounterDTO queryRecord(RequestCounterDTO requestCounterDTO) {
        RequestCounterDOExample example = new RequestCounterDOExample();
        example.createCriteria().andEnvEqualTo(requestCounterDTO.getEnv()).andAppidEqualTo(requestCounterDTO.getAppid())
                .andOpenIdEqualTo(requestCounterDTO.getOpenId()).andTypeEqualTo(requestCounterDTO.getType())
                .andRequestKeyEqualTo(requestCounterDTO.getRequestKey()).andBizDateEqualTo(requestCounterDTO.getBizDate());
        List<RequestCounterDO> result = requestCounterDOMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(result)) {
            return null;
        }
        return do2Dto(result.get(0));
    }


    private RequestCounterDTO do2Dto(RequestCounterDO requestCounterDO) {
        RequestCounterDTO requestCounterDTO = new RequestCounterDTO();
        requestCounterDTO.setId(requestCounterDO.getId());
        requestCounterDTO.setGmtCreate(requestCounterDO.getGmtCreate());
        requestCounterDTO.setGmtModifier(requestCounterDO.getGmtModifier());
        requestCounterDTO.setEnv(requestCounterDO.getEnv());
        requestCounterDTO.setAppid(requestCounterDO.getAppid());
        requestCounterDTO.setOpenId(requestCounterDO.getOpenId());
        requestCounterDTO.setType(requestCounterDO.getType());
        requestCounterDTO.setRequestKey(requestCounterDO.getRequestKey());
        requestCounterDTO.setBizDate(requestCounterDO.getBizDate());
        requestCounterDTO.setLimitNum(requestCounterDO.getLimitNum());
        requestCounterDTO.setRequestNum(requestCounterDO.getRequestNum());
        return requestCounterDTO;
    }

    private RequestCounterDO dto2DO(RequestCounterDTO requestCounterDTO) {
        RequestCounterDO requestCounterDO = new RequestCounterDO();
        requestCounterDO.setId(requestCounterDTO.getId());
        requestCounterDO.setEnv(requestCounterDTO.getEnv());
        requestCounterDO.setAppid(requestCounterDTO.getAppid());
        requestCounterDO.setOpenId(requestCounterDTO.getOpenId());
        requestCounterDO.setType(requestCounterDTO.getType());
        requestCounterDO.setRequestKey(requestCounterDTO.getRequestKey());
        requestCounterDO.setBizDate(requestCounterDTO.getBizDate());
        requestCounterDO.setLimitNum(requestCounterDTO.getLimitNum());
        requestCounterDO.setRequestNum(requestCounterDTO.getRequestNum());
        return requestCounterDO;
    }

    public void invalidate(RequestCounterDTO queryResult) {
        String key = genKey(queryResult);
        localCache.invalidate(key);
    }
}
