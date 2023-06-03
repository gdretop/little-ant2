package com.ant.little.service.msganswer.answerimpl;

import com.ant.little.common.constents.KeyConfigKeyEnum;
import com.ant.little.common.constents.KeyConfigTypeEnum;
import com.ant.little.common.constents.WxMsgTypeEnum;
import com.ant.little.common.model.Response;
import com.ant.little.common.util.DigitalUtil;
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

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: little-ant
 * 描述:
 * @date: 2022/12/27
 * @Version 1.0
 **/
@Component
public class UpdateBoxPositionAnswerService implements MsgAnswerBaseService {

    private final Logger logger = LoggerFactory.getLogger(UpdateBoxPositionAnswerService.class);
    @Autowired
    private KeyConfigService keyConfigService;
    @Autowired
    private AdminConfig adminConfig;

    @Override
    public String getName() {
        return "UpdateBoxPosition";
    }

    @Override
    public boolean isMatch(WxSubMsgDTO wxSubMsgDTO) {
        if (!WxMsgTypeEnum.TEXT.getName().equals(wxSubMsgDTO.getMsgType())) {
            return false;
        }
        if (!wxSubMsgDTO.getContent().startsWith("更新宝箱坐标")) {
            return false;
        }
        if (!adminConfig.isAdmin(wxSubMsgDTO.getWxOpenId())) {
            return false;
        }
        try {
            dataCheck(wxSubMsgDTO.getContent());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("发生异常{}", e.toString(), e);
        }
        return true;
    }

    private int[][] dataCheck(String data) {
        while (data.contains("  ")) {
            data = data.replace("  ", " ");
        }
        Response<int[][]> response = DigitalUtil.parsePoint(data, 2, 4);
        if (response.isFailed()) {
            logger.error("{} {}", response.getErrMsg(), data);
            throw new RuntimeException(response.getErrMsg());
        }
        String[] detail = data.split("\n");
        Character split = DigitalUtil.findFirstNotDigit(detail[1]);
        if (split == null) {
            throw new RuntimeException("日期分隔符无法识别");
        }
        int result[] = DigitalUtil.parseDigit(detail[1], "" + split);
        if (result == null) {
            logger.error("日期格式不正确 {}", detail[1]);
            throw new RuntimeException("日期格式不正确");
        }
        int[][] finalResult = new int[4][];
        finalResult[0] = result;
        for (int i = 0; i < 3; i++) {
            finalResult[i + 1] = response.getData()[i];
        }
        return finalResult;
    }

    @Override
    public Response<WxSubMsgResponseDTO> answer(WxSubMsgDTO wxSubMsgDTO) {
        WxSubMsgResponseDTO wxSubMsgResponseDTO = wxSubMsgDTO.toResponse();
        wxSubMsgResponseDTO.setMsgType(WxMsgTypeEnum.TEXT.getName());
        KeyConfigDTO keyConfigDTO = new KeyConfigDTO();
        keyConfigDTO.setAppid(wxSubMsgDTO.getWxAppid());
        keyConfigDTO.setOpenId(wxSubMsgDTO.getWxOpenId());
        keyConfigDTO.setType(KeyConfigTypeEnum.MORI_GAME.name());
        keyConfigDTO.setKey(KeyConfigKeyEnum.BOX_POSITION.name());
        int[][] result = dataCheck(wxSubMsgDTO.getContent());
        String content = "";
        for (int i = 0; i < 4; i++) {
            if (i > 0) {
                content += "\n";
            }
            content += result[i][0] + "," + result[i][1];
        }
        keyConfigDTO.setValue(content);
        keyConfigService.insert(keyConfigDTO);
        wxSubMsgResponseDTO.setContent("更新成功");
        return Response.newSuccess(wxSubMsgResponseDTO);
    }
}
