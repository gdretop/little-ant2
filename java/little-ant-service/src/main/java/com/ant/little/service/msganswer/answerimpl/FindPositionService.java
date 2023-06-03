package com.ant.little.service.msganswer.answerimpl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ant.little.common.constents.WxMsgTypeEnum;
import com.ant.little.common.model.Response;
import com.ant.little.model.dto.WxSubMsgDTO;
import com.ant.little.model.dto.WxSubMsgResponseDTO;
import com.ant.little.service.findmap.FindMapWayUtil;
import com.ant.little.service.model.FindPositionRequest;
import com.ant.little.service.model.FindPositionResponse;
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
public class FindPositionService implements MsgAnswerBaseService {

    private final Logger logger = LoggerFactory.getLogger(FindPositionService.class);
    @Autowired
    private FindMapWayUtil findMapWayUtil;

    @Override
    public boolean isMatch(WxSubMsgDTO wxSubMsgDTO) {
        if (!WxMsgTypeEnum.JSON.getName().equals(wxSubMsgDTO.getMsgType())) {
            return false;
        }
        if (wxSubMsgDTO.getContent().contains("FindPosition") || wxSubMsgDTO.getContent().contains("坐标查询")) {
            return true;
        }
        return false;
    }

    @Override
    public Response<WxSubMsgResponseDTO> answer(WxSubMsgDTO wxSubMsgDTO) {
        String content = wxSubMsgDTO.getContent();
        FindPositionRequest request = JSONObject.parseObject(content, FindPositionRequest.class);
        FindPositionResponse response = findMapWayUtil.searchPosition(request.getStartX(), request.getStartY(), request.getMatchData());
        WxSubMsgResponseDTO wxSubMsgResponseDTO = wxSubMsgDTO.toResponse();
        wxSubMsgResponseDTO.setMsgType(WxMsgTypeEnum.JSON.getName());
        wxSubMsgResponseDTO.setContent(JSON.toJSONString(response));
        wxSubMsgResponseDTO.setSaveResponse(false);
        return Response.newSuccess(wxSubMsgResponseDTO);
    }

    @Override
    public String getName() {
        return "FindPosition";
    }
}
