package com.ant.little.service.msganswer.answerimpl;

import com.alibaba.fastjson.JSON;
import com.ant.little.common.constents.WxMsgTypeEnum;
import com.ant.little.common.model.Response;
import com.ant.little.model.dto.KeyConfigDTO;
import com.ant.little.model.dto.WxSubMsgDTO;
import com.ant.little.model.dto.WxSubMsgResponseDTO;
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
public class GetKeyConfigAnswerService implements MsgAnswerBaseService {

    private final Logger logger = LoggerFactory.getLogger(GetKeyConfigAnswerService.class);
    @Autowired
    private KeyConfigService keyConfigService;

    @Override
    public String getName() {
        return "GetKeyConfig";
    }

    @Override
    public boolean isMatch(WxSubMsgDTO wxSubMsgDTO) {
        if (!WxMsgTypeEnum.TEXT.getName().equals(wxSubMsgDTO.getMsgType())) {
            return false;
        }
        if (!wxSubMsgDTO.getContent().startsWith("获取配置") && !wxSubMsgDTO.getContent().startsWith("GetKeyConfig")) {
            return false;
        }
        String[] data = wxSubMsgDTO.getContent().split("\n");
        if (data.length != 3) {
            logger.error("参数错误需要type和key两个参数, {}", JSON.toJSONString(wxSubMsgDTO));
            throw new RuntimeException("参数错误需要type和key两个参数");
        }
        return true;
    }

    @Override
    public Response<WxSubMsgResponseDTO> answer(WxSubMsgDTO wxSubMsgDTO) {
        String[] data = wxSubMsgDTO.getContent().split("\n");
        Response<KeyConfigDTO> result = keyConfigService.getKey(data[1], data[2]);
        WxSubMsgResponseDTO wxSubMsgResponseDTO = wxSubMsgDTO.toResponse();
        wxSubMsgResponseDTO.setMsgType(WxMsgTypeEnum.JSON.getName());
        Response<String> response = null;
        if (result.isFailed()) {
            response = Response.newFailure(result.getErrMsg(), "");
        } else if (result.getData() == null) {
            response = Response.newFailure("未查到数据", "");
        } else {
            response = Response.newSuccess(result.getData().getValue());
        }
        wxSubMsgResponseDTO.setContent(JSON.toJSONString(response));
        return Response.newSuccess(wxSubMsgResponseDTO);
    }
}
