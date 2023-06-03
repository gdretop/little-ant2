package com.ant.little.service.msganswer.answerimpl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ant.little.common.constents.WxMsgTypeEnum;
import com.ant.little.common.model.Response;
import com.ant.little.common.util.DateUtil;
import com.ant.little.model.dto.RequestCounterDTO;
import com.ant.little.model.dto.WxSubMsgDTO;
import com.ant.little.model.dto.WxSubMsgResponseDTO;
import com.ant.little.service.msganswer.MsgAnswerBaseService;
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
public class AddRequestNumAnswerService implements MsgAnswerBaseService {

    private final Logger logger = LoggerFactory.getLogger(AddRequestNumAnswerService.class);
    @Autowired
    private RequestCounterService requestCounterService;

    @Override
    public String getName() {
        return "AddRequestNum";
    }

    @Override
    public boolean isMatch(WxSubMsgDTO wxSubMsgDTO) {
        if (!WxMsgTypeEnum.JSON.getName().equals(wxSubMsgDTO.getMsgType())) {
            return false;
        }
        if (!wxSubMsgDTO.getContent().contains("AddRequestNum")) {
            return false;
        }
        return true;
    }

    @Override
    public Response<WxSubMsgResponseDTO> answer(WxSubMsgDTO wxSubMsgDTO) {
        RequestCounterDTO requestCounterDTO = JSON.parseObject(wxSubMsgDTO.getContent(), RequestCounterDTO.class);
        requestCounterDTO.setAppid(wxSubMsgDTO.getWxAppid());
        requestCounterDTO.setOpenId(wxSubMsgDTO.getWxOpenId());
        requestCounterDTO.setBizDate(DateUtil.getDateString("yyyyMMdd"));
        JSONObject jsonObject = JSON.parseObject(wxSubMsgDTO.getContent());
        requestCounterDTO.setRequestKey(jsonObject.getString("requestKey"));
        requestCounterDTO.setType(jsonObject.getString("requestKey"));
        RequestCounterDTO queryResult = requestCounterService.query(requestCounterDTO);
        if (queryResult != null) {
            RequestCounterDTO update = new RequestCounterDTO();
            update.setId(queryResult.getId());
            update.setLimitNum(queryResult.getLimitNum() + 10);
            requestCounterService.updateById(update);
            requestCounterService.invalidate(queryResult);
        }
        WxSubMsgResponseDTO wxSubMsgResponseDTO = wxSubMsgDTO.toResponse();
        wxSubMsgResponseDTO.setMsgType(WxMsgTypeEnum.TEXT.getName());
        wxSubMsgResponseDTO.setContent("更新成功");
        return Response.newSuccess(wxSubMsgResponseDTO);
    }
}
