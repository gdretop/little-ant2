package com.ant.little.service.msganswer.answerimpl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ant.little.common.constents.MemberTypeEnum;
import com.ant.little.common.constents.WxMsgTypeEnum;
import com.ant.little.common.model.Response;
import com.ant.little.model.dto.KeyConfigDTO;
import com.ant.little.model.dto.MemberConfigDTO;
import com.ant.little.model.dto.WxSubMsgDTO;
import com.ant.little.model.dto.WxSubMsgResponseDTO;
import com.ant.little.service.config.AdminConfig;
import com.ant.little.service.member.MemberService;
import com.ant.little.service.msganswer.MsgAnswerBaseService;
import com.ant.little.service.store.KeyConfigService;
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
public class CreateMemberAnswerService implements MsgAnswerBaseService {

    private final Logger logger = LoggerFactory.getLogger(CreateMemberAnswerService.class);
    @Autowired
    private MemberService memberService;
    @Autowired
    private AdminConfig adminConfig;

    @Override
    public String getName() {
        return "CreateMember";
    }

    @Override
    public boolean isMatch(WxSubMsgDTO wxSubMsgDTO) {
        if (!WxMsgTypeEnum.JSON.getName().equals(wxSubMsgDTO.getMsgType())) {
            return false;
        }
        if (!wxSubMsgDTO.getContent().contains("CreateMember")) {
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
        MemberConfigDTO memberConfigDTO = new MemberConfigDTO();
        memberConfigDTO.setAppid(wxSubMsgDTO.getWxAppid());
        memberConfigDTO.setOpenId(wxSubMsgDTO.getWxOpenId());
        memberConfigDTO.setType(MemberTypeEnum.SERVICE_MEMBER.name());
        JSONObject jsonObject = JSON.parseObject(wxSubMsgDTO.getContent());
        memberConfigDTO.setConfigJson(jsonObject);
        Response<MemberConfigDTO> response = memberService.createMemberConfig(memberConfigDTO);
        if (response.isFailed()) {
            return Response.newFailure(response.getErrMsg(), "");
        }
        WxSubMsgResponseDTO wxSubMsgResponseDTO = wxSubMsgDTO.toResponse();
        wxSubMsgResponseDTO.setMsgType(WxMsgTypeEnum.TEXT.getName());
        wxSubMsgResponseDTO.setContent(response.getData().getConfigKey());
        return Response.newSuccess(wxSubMsgResponseDTO);
    }
}
