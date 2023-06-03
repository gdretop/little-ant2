package com.ant.little.service.store;

import com.alibaba.fastjson.JSON;
import com.ant.little.common.model.CommonCacheObj;
import com.ant.little.common.model.Response;
import com.ant.little.core.config.EnvConfig;
import com.ant.little.core.dao.KeyConfigDOMapper;
import com.ant.little.core.domain.KeyConfigDO;
import com.ant.little.core.domain.KeyConfigDOExample;
import com.ant.little.model.dto.KeyConfigDTO;
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
import java.util.stream.Collectors;

/**
 * @author: little-ant
 * 描述:
 * @date: 2022/12/26
 * @Version 1.0
 **/
@Service
public class KeyConfigService {
    private Logger logger = LoggerFactory.getLogger(KeyConfigService.class);
    @Resource
    private KeyConfigDOMapper keyConfigDOMapper;
    @Autowired
    private EnvConfig envConfig;

    private Cache<String, CommonCacheObj> localCache = CacheBuilder.newBuilder()
            .concurrencyLevel(4)
            .initialCapacity(100)
            .expireAfterWrite(3, TimeUnit.DAYS)
//            .expireAfterAccess(3, TimeUnit.DAYS)
            .maximumSize(5000)
            .build();

    public void invalidateAll() {
        localCache.invalidateAll();
    }

    public Response<KeyConfigDTO> insert(KeyConfigDTO keyConfigDTO) {
        String env = envConfig.getCurEnv();
        keyConfigDTO.setEnv(env);

        KeyConfigDO keyConfigDO = dto2DO(keyConfigDTO);
        int effect = keyConfigDOMapper.insertSelective(keyConfigDO);
        if (effect == 1) {
            localCache.invalidateAll();
            return Response.newSuccess(keyConfigDTO);
        } else {
            logger.error("插入信息失败 {}", JSON.toJSONString(keyConfigDTO));
            return Response.newFailure("写入失败", "");
        }
    }

    public Response<KeyConfigDTO> update(String type, String key, String value) {
        Response<KeyConfigDTO> response = getKey(type, key);
        if (response.getData() == null) {
            logger.error("配置不存在 type:{} key:{}", type, key);
            return Response.newFailure(String.format("配置不存在 type:%s key:%s", type, key), "");
        }
        KeyConfigDO keyConfigDO = new KeyConfigDO();
        keyConfigDO.setId(response.getData().getId());
        keyConfigDO.setValue(value);
        int effect = keyConfigDOMapper.updateByPrimaryKeySelective(keyConfigDO);
        if (effect == 0) {
            return Response.newFailure(String.format("更新失败 type:%s key:%s", type, key), "");
        }
        localCache.invalidate(genKey(type, key));
        return getKey(type, key);
    }

    public List<KeyConfigDTO> query(KeyConfigDOExample example) {
        example.getOredCriteria().get(0).andEnvEqualTo(envConfig.getCurEnv());
        List<KeyConfigDO> result = keyConfigDOMapper.selectByExample(example);
        List<KeyConfigDTO> response = result.stream().map(this::do2Dto).collect(Collectors.toList());
        return response;
    }

    public Response<KeyConfigDTO> getKey(String type, String key) {
        String cacheKey = genKey(type, key);
        CommonCacheObj<KeyConfigDTO> cacheObj = localCache.getIfPresent(cacheKey);
        if (cacheObj != null) {
            if (cacheObj.data != null) {
                return Response.newSuccess(cacheObj.data);
            }
            return Response.newSuccess(null);
        }
        KeyConfigDOExample example = new KeyConfigDOExample();
        example.createCriteria().andTypeEqualTo(type).andConfigKeyEqualTo(key).andEnvEqualTo(envConfig.getCurEnv());
        example.setOrderByClause(" gmt_create desc limit 1");
        List<KeyConfigDO> result = keyConfigDOMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(result)) {
            localCache.put(cacheKey, new CommonCacheObj());
            return Response.newSuccess(null);
        }
        KeyConfigDTO keyConfigDTO = do2Dto(result.get(0));
        CommonCacheObj<KeyConfigDTO> cacheObj1 = new CommonCacheObj(keyConfigDTO);
        localCache.put(cacheKey, cacheObj1);
        return Response.newSuccess(keyConfigDTO);
    }

    public String genKey(String type, String key) {
        String cacheKey = String.format("%s#%s", type, key);
        return cacheKey;
    }


    private KeyConfigDTO do2Dto(KeyConfigDO keyConfigDO) {
        KeyConfigDTO keyConfigDTO = new KeyConfigDTO();
        keyConfigDTO.setId(keyConfigDO.getId());
        keyConfigDTO.setGmtCreate(keyConfigDO.getGmtCreate());
        keyConfigDTO.setGmtModifier(keyConfigDO.getGmtModifier());
        keyConfigDTO.setEnv(keyConfigDO.getEnv());
        keyConfigDTO.setAppid(keyConfigDO.getAppid());
        keyConfigDTO.setOpenId(keyConfigDO.getOpenId());
        keyConfigDTO.setType(keyConfigDO.getType());
        keyConfigDTO.setKey(keyConfigDO.getConfigKey());
        keyConfigDTO.setValue(keyConfigDO.getValue());
        return keyConfigDTO;
    }

    private KeyConfigDO dto2DO(KeyConfigDTO keyConfigDTO) {
        KeyConfigDO keyConfigDO = new KeyConfigDO();
        keyConfigDO.setId(keyConfigDTO.getId());
        keyConfigDO.setEnv(keyConfigDTO.getEnv());
        keyConfigDO.setAppid(keyConfigDTO.getAppid());
        keyConfigDO.setOpenId(keyConfigDTO.getOpenId());
        keyConfigDO.setType(keyConfigDTO.getType());
        keyConfigDO.setConfigKey(keyConfigDTO.getKey());
        keyConfigDO.setValue(keyConfigDTO.getValue());
        return keyConfigDO;
    }
}
