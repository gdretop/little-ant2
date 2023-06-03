package com.ant.little.service.store;

import com.alibaba.fastjson.JSON;
import com.ant.little.common.model.Response;
import com.ant.little.core.config.EnvConfig;
import com.ant.little.core.dao.WxUserDOMapper;
import com.ant.little.core.domain.WxUserDO;
import com.ant.little.core.domain.WxUserDOExample;
import com.ant.little.model.dto.WxUserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: little-ant
 * 描述:
 * @date: 2022/12/26
 * @Version 1.0
 **/
@Service
public class WxUserService {
    private Logger logger = LoggerFactory.getLogger(WxUserService.class);
    @Resource
    private WxUserDOMapper wxUserDOMapper;
    @Autowired
    private EnvConfig envConfig;

    public Response<WxUserDTO> upsert(WxUserDTO wxUserDTO) {
        String env = envConfig.getCurEnv();
        wxUserDTO.setEnv(env);
        WxUserDOExample wxUserDOExample = new WxUserDOExample();
        WxUserDOExample.Criteria criteria = wxUserDOExample.createCriteria();
        criteria.andAppidEqualTo(wxUserDTO.getAppid()).andEnvEqualTo(wxUserDTO.getEnv()).andOpenIdEqualTo(wxUserDTO.getOpenId());
        List<WxUserDO> result = wxUserDOMapper.selectByExample(wxUserDOExample);
        if (!CollectionUtils.isEmpty(result)) {
            wxUserDTO.setId(result.get(0).getId());
            WxUserDO wxUserDO = dto2DO(wxUserDTO);
            int effect = wxUserDOMapper.updateByPrimaryKeySelective(wxUserDO);
            if (effect == 1) {
                return Response.newSuccess(wxUserDTO);
            } else {
                logger.error("更新id={}信息失败 {}", wxUserDTO.getId(), JSON.toJSONString(wxUserDTO));
                return Response.newFailure("更新失败", "");
            }
        } else {
            WxUserDO wxUserDO = dto2DO(wxUserDTO);
            int effect = wxUserDOMapper.insertSelective(wxUserDO);
            if (effect == 1) {
                return Response.newSuccess(wxUserDTO);
            } else {
                logger.error("插入信息失败 {}", JSON.toJSONString(wxUserDTO));
                return Response.newFailure("写入失败", "");
            }
        }
    }

    private WxUserDO dto2DO(WxUserDTO wxUserDTO) {
        WxUserDO wxUserDO = new WxUserDO();
        wxUserDO.setId(wxUserDTO.getId());
        wxUserDO.setOpenId(wxUserDTO.getOpenId());
        wxUserDO.setPhone(wxUserDTO.getPhone());
        wxUserDO.setUserName(wxUserDTO.getUserName());
        wxUserDO.setGender(wxUserDTO.getGender());
        wxUserDO.setAvatarUrl(wxUserDTO.getAvatarUrl());
        wxUserDO.setRole(wxUserDTO.getRole());
        wxUserDO.setEnv(wxUserDTO.getEnv());
        wxUserDO.setAppid(wxUserDTO.getAppid());
        wxUserDO.setLastLocation(wxUserDTO.getLastLocation());
        wxUserDO.setLastSource(wxUserDTO.getLastSource());
        return wxUserDO;
    }
}
