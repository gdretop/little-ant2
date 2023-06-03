package com.ant.little.service.msganswer.answerimpl;

import com.alibaba.fastjson.JSON;
import com.ant.little.common.constents.KeyConfigTypeEnum;
import com.ant.little.common.constents.WxMsgTypeEnum;
import com.ant.little.common.model.Response;
import com.ant.little.core.domain.KeyConfigDOExample;
import com.ant.little.model.dto.KeyConfigDTO;
import com.ant.little.model.dto.WxSubMsgDTO;
import com.ant.little.model.dto.WxSubMsgResponseDTO;
import com.ant.little.service.config.AdminConfig;
import com.ant.little.service.msganswer.MsgAnswerBaseService;
import com.ant.little.service.store.KeyConfigService;
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
public class QueryMemberConfigAnswerService implements MsgAnswerBaseService {

    private final Logger logger = LoggerFactory.getLogger(QueryMemberConfigAnswerService.class);
    @Autowired
    private KeyConfigService keyConfigService;
    @Autowired
    private AdminConfig adminConfig;

    @Override
    public String getName() {
        return "QueryMemberConfig";
    }

    @Override
    public boolean isMatch(WxSubMsgDTO wxSubMsgDTO) {
        if (!WxMsgTypeEnum.JSON.getName().equals(wxSubMsgDTO.getMsgType())) {
            return false;
        }
        if (!wxSubMsgDTO.getContent().contains("QueryMemberConfig")) {
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
        KeyConfigDOExample example = new KeyConfigDOExample();
        example.createCriteria().andTypeIn(Arrays.asList(KeyConfigTypeEnum.VIP.name(),
                KeyConfigTypeEnum.NORMAL.name(), KeyConfigTypeEnum.gh_d578112e1577.name()));
        List<KeyConfigDTO> result = keyConfigService.query(example);
        WxSubMsgResponseDTO wxSubMsgResponseDTO = wxSubMsgDTO.toResponse();
        wxSubMsgResponseDTO.setMsgType(WxMsgTypeEnum.JSON.getName());
        Response response = Response.newSuccess(result);
        wxSubMsgResponseDTO.setContent(JSON.toJSONString(response));
        return Response.newSuccess(wxSubMsgResponseDTO);
    }
}
