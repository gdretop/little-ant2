package com.ant.little.service.msganswer.answerimpl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ant.little.common.constents.MemberTypeEnum;
import com.ant.little.common.constents.WxMsgTypeEnum;
import com.ant.little.common.model.Response;
import com.ant.little.model.dto.MemberConfigDTO;
import com.ant.little.model.dto.WxSubMsgDTO;
import com.ant.little.model.dto.WxSubMsgResponseDTO;
import com.ant.little.service.config.AdminConfig;
import com.ant.little.service.member.MemberService;
import com.ant.little.service.msganswer.MsgAnswerBaseService;
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
public class BindMemberAnswerService implements MsgAnswerBaseService {

    private final Logger logger = LoggerFactory.getLogger(BindMemberAnswerService.class);
    @Autowired
    private MemberService memberService;

    @Override
    public String getName() {
        return "BindMember";
    }

    @Override
    public boolean isMatch(WxSubMsgDTO wxSubMsgDTO) {
        if (!WxMsgTypeEnum.JSON.getName().equals(wxSubMsgDTO.getMsgType())) {
            return false;
        }
        if (!wxSubMsgDTO.getContent().contains("BindMember")) {
            return false;
        }
        return true;
    }

    @Override
    public Response<WxSubMsgResponseDTO> answer(WxSubMsgDTO wxSubMsgDTO) {
        MemberConfigDTO memberConfigDTO = new MemberConfigDTO();
        memberConfigDTO.setAppid(wxSubMsgDTO.getWxAppid());
        memberConfigDTO.setOpenId(wxSubMsgDTO.getWxOpenId());
        memberConfigDTO.setType(MemberTypeEnum.SERVICE_MEMBER.name());
        JSONObject jsonObject = JSON.parseObject(wxSubMsgDTO.getContent());
        memberConfigDTO.setConfigKey(jsonObject.getString("configKey"));
        Response<MemberConfigDTO> response = memberService.bindMember(memberConfigDTO);
        if (response.isFailed()) {
            return Response.newFailure(response.getErrMsg(), "");
        }
        WxSubMsgResponseDTO wxSubMsgResponseDTO = wxSubMsgDTO.toResponse();
        wxSubMsgResponseDTO.setMsgType(WxMsgTypeEnum.TEXT.getName());
        wxSubMsgResponseDTO.setContent("绑定成功");
        return Response.newSuccess(wxSubMsgResponseDTO);
    }
}
