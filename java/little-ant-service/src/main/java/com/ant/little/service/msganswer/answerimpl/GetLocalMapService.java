package com.ant.little.service.msganswer.answerimpl;

import com.alibaba.fastjson.JSONObject;
import com.ant.little.common.constents.WxMsgTypeEnum;
import com.ant.little.common.model.Point;
import com.ant.little.common.model.Response;
import com.ant.little.model.dto.WxSubMsgDTO;
import com.ant.little.model.dto.WxSubMsgResponseDTO;
import com.ant.little.service.findmap.FindMapWayUtil;
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
public class GetLocalMapService implements MsgAnswerBaseService {

    private final Logger logger = LoggerFactory.getLogger(GetLocalMapService.class);
    @Autowired
    private FindMapWayUtil findMapWayUtil;

    @Override
    public String getName() {
        return "GetLocalMap";
    }

    @Override
    public boolean isMatch(WxSubMsgDTO wxSubMsgDTO) {
        if (!WxMsgTypeEnum.JSON.getName().equals(wxSubMsgDTO.getMsgType())) {
            return false;
        }
        if (wxSubMsgDTO.getContent().contains("局部图") || wxSubMsgDTO.getContent().contains("GetLocalMap")) {
            return true;
        }
        return false;
    }

    @Override
    public Response<WxSubMsgResponseDTO> answer(WxSubMsgDTO wxSubMsgDTO) {
        String content = wxSubMsgDTO.getContent();
        JSONObject jsonObject = JSONObject.parseObject(content);
        Point point = new Point();
        point.x = jsonObject.getInteger("x");
        point.y = jsonObject.getInteger("y");
        String image = findMapWayUtil.genLocalMap(point);
        WxSubMsgResponseDTO wxSubMsgResponseDTO = wxSubMsgDTO.toResponse();
        wxSubMsgResponseDTO.setMsgType(WxMsgTypeEnum.TEXT.getName());
        wxSubMsgResponseDTO.setContent(image);
        wxSubMsgResponseDTO.setSaveResponse(false);
        return Response.newSuccess(wxSubMsgResponseDTO);
    }
}
