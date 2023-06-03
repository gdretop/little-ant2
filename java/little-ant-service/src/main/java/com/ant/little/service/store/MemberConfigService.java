package com.ant.little.service.store;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ant.little.common.model.CommonCacheObj;
import com.ant.little.common.model.Response;
import com.ant.little.common.util.Md5Util;
import com.ant.little.core.config.EnvConfig;
import com.ant.little.core.dao.MemberConfigDOMapper;
import com.ant.little.core.domain.MemberConfigDO;
import com.ant.little.core.domain.MemberConfigDOExample;
import com.ant.little.core.domain.MemberInfoDO;
import com.ant.little.core.domain.MemberInfoDOExample;
import com.ant.little.model.dto.MemberConfigDTO;
import com.ant.little.model.dto.MemberInfoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author: little-ant
 * 描述:
 * @date: 2022/12/26
 * @Version 1.0
 **/
@Service
public class MemberConfigService {
    private Logger logger = LoggerFactory.getLogger(MemberConfigService.class);
    @Autowired
    private MemberConfigDOMapper memberConfigDOMapper;
    @Autowired
    private EnvConfig envConfig;

    /**
     * 插入一条会员记录
     *
     * @param memberConfigDTO
     * @return
     */
    public Response<MemberConfigDTO> insert(MemberConfigDTO memberConfigDTO) {
        String env = envConfig.getCurEnv();
        memberConfigDTO.setEnv(env);
        long now = System.currentTimeMillis();
        try {
            String key = Md5Util.md5(now + "");
            memberConfigDTO.setConfigKey(key);
        } catch (Exception e) {
            logger.error("生成会员信息失败 {}", e.toString(), e);
            return Response.newFailure("生成会员信息失败", "");
        }
        memberConfigDTO.setIsBand(0);
        memberConfigDTO.setBandOpenId(null);
        MemberConfigDO memberConfigDO = dto2DO(memberConfigDTO);
        int effect = memberConfigDOMapper.insertSelective(memberConfigDO);
        if (effect == 1) {
            return Response.newSuccess(memberConfigDTO);
        } else {
            logger.error("插入信息失败 {}", JSON.toJSONString(memberConfigDTO));
            return Response.newFailure("写入失败", "");
        }
    }

    /**
     * 查询会员配置
     *
     * @param type
     * @param key
     * @return
     */
    public MemberConfigDTO query(String type, String key) {
        String env = envConfig.getCurEnv();
        MemberConfigDOExample example = new MemberConfigDOExample();
        example.createCriteria().andEnvEqualTo(env).andTypeEqualTo(type).andConfigKeyEqualTo(key);
        List<MemberConfigDO> result = memberConfigDOMapper.selectByExample(example);
        if (!CollectionUtils.isEmpty(result)) {
            MemberConfigDO memberConfigDO = result.get(0);
            MemberConfigDTO response = do2DTO(memberConfigDO);
            return response;
        }
        return null;
    }

    /**
     * 更新会员配置
     *
     * @param memberConfigDTO 必须要有id
     * @return
     */
    public Response<MemberConfigDTO> update(MemberConfigDTO memberConfigDTO) {
        MemberConfigDO memberConfigDO = dto2DO(memberConfigDTO);
        int effect = memberConfigDOMapper.updateByPrimaryKeySelective(memberConfigDO);
        if (effect == 1) {
            return Response.newSuccess(memberConfigDTO);
        } else {
            return Response.newFailure("更新失败", "");
        }
    }

    private MemberConfigDO dto2DO(MemberConfigDTO memberConfigDTO) {
        MemberConfigDO memberConfigDO = new MemberConfigDO();
        memberConfigDO.setId(memberConfigDTO.getId());
        memberConfigDO.setEnv(memberConfigDTO.getEnv());
        memberConfigDO.setAppid(memberConfigDTO.getAppid());
        memberConfigDO.setOpenId(memberConfigDTO.getOpenId());
        memberConfigDO.setType(memberConfigDTO.getType());
        if (memberConfigDTO.getConfigJson() != null) {
            memberConfigDO.setConfigJson(JSON.toJSONString(memberConfigDTO.getConfigJson()));
        }
        memberConfigDO.setConfigKey(memberConfigDTO.getConfigKey());
        memberConfigDO.setIsBand(memberConfigDTO.getIsBand());
        memberConfigDO.setBandOpenId(memberConfigDTO.getBandOpenId());
        return memberConfigDO;
    }

    private MemberConfigDTO do2DTO(MemberConfigDO memberConfigDO) {
        MemberConfigDTO memberConfigDTO = new MemberConfigDTO();
        memberConfigDTO.setId(memberConfigDO.getId());
        memberConfigDTO.setGmtCreate(memberConfigDO.getGmtCreate());
        memberConfigDTO.setGmtModifier(memberConfigDO.getGmtModifier());
        memberConfigDTO.setEnv(memberConfigDO.getEnv());
        memberConfigDTO.setAppid(memberConfigDO.getAppid());
        memberConfigDTO.setOpenId(memberConfigDO.getOpenId());
        memberConfigDTO.setType(memberConfigDO.getType());
        if (memberConfigDO.getConfigJson() != null) {
            memberConfigDTO.setConfigJson(JSON.parseObject(memberConfigDO.getConfigJson()));
        } else {
            memberConfigDTO.setConfigJson(new JSONObject());
        }
        memberConfigDTO.setIsBand(memberConfigDO.getIsBand());
        memberConfigDTO.setBandOpenId(memberConfigDO.getBandOpenId());
        return memberConfigDTO;
    }


}
