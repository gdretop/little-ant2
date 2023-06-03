package com.ant.little.service.msganswer.answerimpl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ant.little.common.constents.KeyConfigTypeEnum;
import com.ant.little.common.constents.MemberTypeEnum;
import com.ant.little.common.constents.WxMsgTypeEnum;
import com.ant.little.common.model.Response;
import com.ant.little.core.domain.KeyConfigDOExample;
import com.ant.little.model.dto.*;
import com.ant.little.service.config.AdminConfig;
import com.ant.little.service.member.MemberService;
import com.ant.little.service.model.UserInfo;
import com.ant.little.service.msganswer.MsgAnswerBaseService;
import com.ant.little.service.store.KeyConfigService;
import com.ant.little.service.store.MemberInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @author: little-ant
 * 描述:
 * @date: 2022/12/27
 * @Version 1.0
 **/
@Component
public class QueryUserInfoAnswerService implements MsgAnswerBaseService {

    private final Logger logger = LoggerFactory.getLogger(QueryUserInfoAnswerService.class);
    @Autowired
    private MemberInfoService memberInfoService;
    @Autowired
    private AdminConfig adminConfig;
    @Autowired
    private KeyConfigService keyConfigService;

    @Override
    public String getName() {
        return "QueryUserInfo";
    }

    @Override
    public boolean isMatch(WxSubMsgDTO wxSubMsgDTO) {
        if (!WxMsgTypeEnum.JSON.getName().equals(wxSubMsgDTO.getMsgType())) {
            return false;
        }
        if (!wxSubMsgDTO.getContent().contains("QueryUserInfo")) {
            return false;
        }
        return true;
    }

    @Override
    public Response<WxSubMsgResponseDTO> answer(WxSubMsgDTO wxSubMsgDTO) {
        MemberInfoDTO memberInfoDTO = new MemberInfoDTO();
        memberInfoDTO.setAppid(wxSubMsgDTO.getWxAppid());
        memberInfoDTO.setOpenId(wxSubMsgDTO.getWxOpenId());
        memberInfoDTO.setType(MemberTypeEnum.SERVICE_MEMBER.name());
        MemberInfoDTO queryResult = memberInfoService.query(memberInfoDTO);
        UserInfo userInfo = new UserInfo();
        if (queryResult != null) {
            userInfo = JSONObject.parseObject(queryResult.getConfigJson(), UserInfo.class);
        }
        if (adminConfig.isAdmin(wxSubMsgDTO.getWxOpenId())) {
            userInfo.setAdmin(true);
        }
        KeyConfigDOExample example = new KeyConfigDOExample();
        example.createCriteria().andTypeIn(Arrays.asList(KeyConfigTypeEnum.VIP.name(),
                KeyConfigTypeEnum.NORMAL.name(), KeyConfigTypeEnum.gh_d578112e1577.name()));
        List<KeyConfigDTO> result = keyConfigService.query(example);
        for (KeyConfigDTO keyConfigDTO : result) {
            String key = "waitTime";
            String type = "normal";
            if (userInfo.isVip()) {
                type = "vip";
            }
            if (keyConfigDTO.getKey().equals(key) && keyConfigDTO.getType().equals(type)) {
                userInfo.setWaitTime(Integer.parseInt(keyConfigDTO.getValue()));
                break;
            }
        }
        WxSubMsgResponseDTO wxSubMsgResponseDTO = wxSubMsgDTO.toResponse();
        wxSubMsgResponseDTO.setMsgType(WxMsgTypeEnum.JSON.getName());
        wxSubMsgResponseDTO.setContent(JSON.toJSONString(userInfo));
        return Response.newSuccess(wxSubMsgResponseDTO);
    }
}
