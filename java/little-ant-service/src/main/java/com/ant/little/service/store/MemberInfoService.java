package com.ant.little.service.store;

import com.alibaba.fastjson.JSON;
import com.ant.little.common.model.CommonCacheObj;
import com.ant.little.common.model.Response;
import com.ant.little.core.config.EnvConfig;
import com.ant.little.core.dao.MemberInfoDOMapper;
import com.ant.little.core.domain.MemberInfoDO;
import com.ant.little.core.domain.MemberInfoDOExample;
import com.ant.little.model.dto.MemberInfoDTO;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author: little-ant
 * 描述:
 * @date: 2022/12/26
 * @Version 1.0
 **/
@Service
public class MemberInfoService {
    private Logger logger = LoggerFactory.getLogger(MemberInfoService.class);
    @Autowired
    private MemberInfoDOMapper memberInfoDOMapper;
    @Autowired
    private EnvConfig envConfig;
    private Cache<String, CommonCacheObj<MemberInfoDTO>> localCache = CacheBuilder.newBuilder()
            .concurrencyLevel(4)
            .initialCapacity(100)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(5000)
            .build();

    /**
     * 写入一条会员信息
     *
     * @param memberInfoDTO
     * @return
     */
    public Response<MemberInfoDTO> insert(MemberInfoDTO memberInfoDTO) {
        String env = envConfig.getCurEnv();
        memberInfoDTO.setEnv(env);
        MemberInfoDO memberInfoDO = dto2DO(memberInfoDTO);
        int effect = memberInfoDOMapper.insertSelective(memberInfoDO);
        if (effect == 1) {
            MemberInfoDTO result = query(memberInfoDTO);
            return Response.newSuccess(result);
        } else {
            logger.error("插入信息失败 {}", JSON.toJSONString(memberInfoDTO));
            return Response.newFailure("写入失败", "");
        }
    }

    public void invalidateAll() {
        localCache.invalidateAll();
    }

    /**
     * 失效会员信息，并清理缓存,同时重新加载
     *
     * @param memberInfoDTO
     * @return
     */
    public Response<MemberInfoDTO> inValidate(MemberInfoDTO memberInfoDTO) {
        String env = envConfig.getCurEnv();
        memberInfoDTO.setEnv(env);
        MemberInfoDO update = new MemberInfoDO();
        update.setId(memberInfoDTO.getId());
        update.setIsValid(0);
        memberInfoDOMapper.updateByPrimaryKeySelective(update);
        String key = genKey(memberInfoDTO);
        localCache.invalidate(key);
        return Response.newSuccess(memberInfoDTO);
    }

    /**
     * 查询会员信息
     *
     * @param memberInfoDTO
     * @return
     */
    public MemberInfoDTO query(MemberInfoDTO memberInfoDTO) {
        String env = envConfig.getCurEnv();
        memberInfoDTO.setEnv(env);
        String key = genKey(memberInfoDTO);
        CommonCacheObj<MemberInfoDTO> commonCacheObj = localCache.getIfPresent(key);
        if (commonCacheObj != null && commonCacheObj.data != null) {
            return commonCacheObj.data;
        }
        MemberInfoDOExample example = new MemberInfoDOExample();
        example.createCriteria().andTypeEqualTo(memberInfoDTO.getType()).andAppidEqualTo(memberInfoDTO.getAppid())
                .andOpenIdEqualTo(memberInfoDTO.getOpenId()).andIsValidEqualTo(1).andEnvEqualTo(memberInfoDTO.getEnv());
        example.setOrderByClause(" end_time desc");
        List<MemberInfoDO> result = memberInfoDOMapper.selectByExample(example);
        if (!CollectionUtils.isEmpty(result)) {
            MemberInfoDO memberInfoDO = result.get(0);
            MemberInfoDTO response = do2DTO(memberInfoDO);
            localCache.put(key, new CommonCacheObj<>(response));
            return response;
        }
        return null;
    }

    private String genKey(MemberInfoDTO memberInfoDTO) {
        return String.format("%s-%s-%s-%s", memberInfoDTO.getEnv(), memberInfoDTO.getAppid()
                , memberInfoDTO.getOpenId(), memberInfoDTO.getType());
    }

    private MemberInfoDO dto2DO(MemberInfoDTO memberInfoDTO) {
        MemberInfoDO memberInfoDO = new MemberInfoDO();
        memberInfoDO.setId(memberInfoDTO.getId());
        memberInfoDO.setEnv(memberInfoDTO.getEnv());
        memberInfoDO.setAppid(memberInfoDTO.getAppid());
        memberInfoDO.setOpenId(memberInfoDTO.getOpenId());
        memberInfoDO.setType(memberInfoDTO.getType());
        memberInfoDO.setConfigJson(memberInfoDTO.getConfigJson());
        memberInfoDO.setBindConfigKey(memberInfoDTO.getBindConfigKey());
        memberInfoDO.setStartTime(memberInfoDTO.getStartTime());
        memberInfoDO.setEndTime(memberInfoDTO.getEndTime());
        memberInfoDO.setIsValid(memberInfoDTO.getIsValid());
        return memberInfoDO;
    }

    private MemberInfoDTO do2DTO(MemberInfoDO memberInfoDO) {
        MemberInfoDTO memberInfoDTO = new MemberInfoDTO();
        memberInfoDTO.setId(memberInfoDO.getId());
        memberInfoDTO.setGmtCreate(memberInfoDO.getGmtCreate());
        memberInfoDTO.setGmtModifier(memberInfoDO.getGmtModifier());
        memberInfoDTO.setEnv(memberInfoDO.getEnv());
        memberInfoDTO.setAppid(memberInfoDO.getAppid());
        memberInfoDTO.setOpenId(memberInfoDO.getOpenId());
        memberInfoDTO.setType(memberInfoDO.getType());
        memberInfoDTO.setConfigJson(memberInfoDO.getConfigJson());
        memberInfoDTO.setBindConfigKey(memberInfoDO.getBindConfigKey());
        memberInfoDTO.setStartTime(memberInfoDO.getStartTime());
        memberInfoDTO.setEndTime(memberInfoDO.getEndTime());
        memberInfoDTO.setIsValid(memberInfoDO.getIsValid());
        return memberInfoDTO;
    }


}
