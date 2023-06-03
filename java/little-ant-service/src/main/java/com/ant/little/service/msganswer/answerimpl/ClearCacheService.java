package com.ant.little.service.msganswer.answerimpl;

import com.alibaba.fastjson.JSON;
import com.ant.little.common.constents.WxMsgTypeEnum;
import com.ant.little.common.model.Response;
import com.ant.little.model.dto.WxSubMsgDTO;
import com.ant.little.model.dto.WxSubMsgResponseDTO;
import com.ant.little.service.config.AdminConfig;
import com.ant.little.service.msganswer.MsgAnswerBaseService;
import com.ant.little.service.store.KeyConfigService;
import com.ant.little.service.store.MemberInfoService;
import com.ant.little.service.store.RequestCounterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: little-ant
 * 描述:
 * @date: 2022/12/27
 * @Version 1.0
 **/
@Component
public class ClearCacheService implements MsgAnswerBaseService {

    private final Logger logger = LoggerFactory.getLogger(ClearCacheService.class);
    @Autowired
    private KeyConfigService keyConfigService;
    @Autowired
    private RequestCounterService requestCounterService;
    @Autowired
    private MemberInfoService memberInfoService;
    @Autowired
    private AdminConfig adminConfig;


    @Override
    public String getName() {
        return "ClearCache";
    }

    @Override
    public boolean isMatch(WxSubMsgDTO wxSubMsgDTO) {
        if (!WxMsgTypeEnum.JSON.getName().equals(wxSubMsgDTO.getMsgType())) {
            return false;
        }
        if (!wxSubMsgDTO.getContent().contains("ClearCache")) {
            return false;
        }
        return true;
    }

    @Override
    public Response<WxSubMsgResponseDTO> answer(WxSubMsgDTO wxSubMsgDTO) {
        if (!adminConfig.isAdmin(wxSubMsgDTO.getWxOpenId())) {
            logger.error("非法访问 {}", JSON.toJSONString(wxSubMsgDTO));
            return Response.newFailure("无权访问", "");
        }
        keyConfigService.invalidateAll();
        memberInfoService.invalidateAll();
        requestCounterService.invalidateAll();
        WxSubMsgResponseDTO wxSubMsgResponseDTO = wxSubMsgDTO.toResponse();
        wxSubMsgResponseDTO.setMsgType(WxMsgTypeEnum.TEXT.getName());
        wxSubMsgResponseDTO.setContent("清理成功");
        return Response.newSuccess(wxSubMsgResponseDTO);
    }
}
