package com.ant.little.service.msganswer.answerimpl;

import com.ant.little.common.constents.WxMsgTypeEnum;
import com.ant.little.common.model.Response;
import com.ant.little.core.config.EnvConfig;
import com.ant.little.model.dto.WxSubMsgDTO;
import com.ant.little.model.dto.WxSubMsgResponseDTO;
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
public class SubscribeService implements MsgAnswerBaseService {

    private final Logger logger = LoggerFactory.getLogger(SubscribeService.class);
    private final static String CONTENT = "欢迎关注旺仔小蚂蚁!\n" +
            "使用末日生存地图工具 点击菜单栏->生存之路 查看教程和工具地址";
    @Autowired
    private EnvConfig envConfig;

    @Override
    public String getName() {
        return "Subscribe";
    }

    @Override
    public boolean isMatch(WxSubMsgDTO wxSubMsgDTO) {
        if (!WxMsgTypeEnum.EVENT.getName().equals(wxSubMsgDTO.getMsgType())) {
            return false;
        }
        if (!wxSubMsgDTO.getEvent().equals("subscribe")) {
            return false;
        }
        return true;
    }

    public Response<WxSubMsgResponseDTO> answer(WxSubMsgDTO wxSubMsgDTO) {
        WxSubMsgResponseDTO wxSubMsgResponseDTO = wxSubMsgDTO.toResponse();
        wxSubMsgResponseDTO.setMsgType(WxMsgTypeEnum.TEXT.getName());
        wxSubMsgResponseDTO.setContent(CONTENT);
        return Response.newSuccess(wxSubMsgResponseDTO);
    }
}
