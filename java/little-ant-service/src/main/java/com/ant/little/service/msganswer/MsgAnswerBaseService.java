package com.ant.little.service.msganswer;

import com.ant.little.common.model.Response;
import com.ant.little.model.dto.WxSubMsgDTO;
import com.ant.little.model.dto.WxSubMsgResponseDTO;

/**
 * @author: little-ant
 * 描述:
 * @date: 2022/12/27
 * @Version 1.0
 **/
public interface MsgAnswerBaseService {
    /**
     * 是否匹配当前输入
     *
     * @param wxSubMsgDTO
     * @return
     */
    boolean isMatch(WxSubMsgDTO wxSubMsgDTO);

    /**
     * 回复消息
     *
     * @param wxSubMsgDTO
     * @return
     */
    Response<WxSubMsgResponseDTO> answer(WxSubMsgDTO wxSubMsgDTO);

    /**
     * 获取服务名
     *
     * @return
     */
    String getName();
}
