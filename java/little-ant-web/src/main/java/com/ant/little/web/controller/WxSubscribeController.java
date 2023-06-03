package com.ant.little.web.controller;

import com.alibaba.fastjson.JSON;
import com.ant.little.common.constents.ResponseTemplateConstants;
import com.ant.little.common.constents.WxMsgTypeEnum;
import com.ant.little.common.model.Response;
import com.ant.little.model.dto.WxSubMsgDTO;
import com.ant.little.model.dto.WxSubMsgResponseDTO;
import com.ant.little.service.msganswer.MsgAnswerRouter;
import com.ant.little.web.vo.WxMsgVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 公众号消息回复
 */
@RestController
@RequestMapping(value = "/api/wxsubscribe")
public class WxSubscribeController {

    final Logger logger = LoggerFactory.getLogger(WxSubscribeController.class);
    @Resource
    private HttpServletRequest httpServletRequest;
    @Autowired
    private MsgAnswerRouter msgAnswerRouter;

    public WxSubscribeController() {
    }

    @PostMapping(value = "/answer")
    @ResponseBody
    public WxMsgVO answer(@RequestBody WxMsgVO wxMsgVO) {
        long startTime = System.currentTimeMillis();
        try {
            WxSubMsgDTO wxSubMsgDTO = vo2Dto(wxMsgVO);
            logger.info("接收到请求 {}", JSON.toJSONString(wxSubMsgDTO));
            Response<WxSubMsgResponseDTO> response = msgAnswerRouter.process(wxSubMsgDTO);
            if (response.isFailed()) {
                WxMsgVO result = wxMsgVO.transDirection();
                result.MsgType = (WxMsgTypeEnum.TEXT.getName());
                result.Content = (response.getErrMsg());
                return result;
            }
            return tans2WxResponse(response.getData());
        } catch (Exception e) {
            logger.error("处理异常{}", e.toString(), e);
            WxMsgVO response = wxMsgVO.transDirection();
            response.MsgType = WxMsgTypeEnum.TEXT.getName();
            response.Content = ResponseTemplateConstants.SERVER_ERROR;
            return response;
        } finally {
            long endTime = System.currentTimeMillis();
            logger.info("此次执行消耗时间: {}", endTime - startTime);
        }
    }

    private WxSubMsgDTO vo2Dto(WxMsgVO wxMsgVO) {
        WxSubMsgDTO wxSubMsgDTO = new WxSubMsgDTO();
        wxSubMsgDTO.setToUserName(wxMsgVO.ToUserName);
        wxSubMsgDTO.setFromUserName(wxMsgVO.FromUserName);
        wxSubMsgDTO.setCreateTime(wxMsgVO.CreateTime);
        wxSubMsgDTO.setMsgType(wxMsgVO.MsgType);
        wxSubMsgDTO.setContent(wxMsgVO.Content);
        wxSubMsgDTO.setMsgId(wxMsgVO.MsgId);
        wxSubMsgDTO.setEvent(wxMsgVO.Event);
        wxSubMsgDTO.setEventKey(wxMsgVO.EventKey);
        wxSubMsgDTO.setTicket(wxMsgVO.Ticket);
        wxSubMsgDTO.setLatitude(wxMsgVO.Latitude);
        wxSubMsgDTO.setLongitude(wxMsgVO.Longitude);
        wxSubMsgDTO.setPrecision(wxMsgVO.Precision);
        wxSubMsgDTO.setWxOpenId(httpServletRequest.getHeader("x-wx-from-openid"));
        wxSubMsgDTO.setWxSource(httpServletRequest.getHeader("x-wx-source"));
        wxSubMsgDTO.setRealIp(httpServletRequest.getHeader("x-real-ip"));
        wxSubMsgDTO.setWxAppid(httpServletRequest.getHeader("x-wx-appid"));
        return wxSubMsgDTO;
    }

    private WxMsgVO tans2WxResponse(WxSubMsgResponseDTO wxSubMsgResponseDTO) {
        WxMsgVO wxMsgVO = new WxMsgVO();
        wxMsgVO.ToUserName = wxSubMsgResponseDTO.getToUserName();
        wxMsgVO.FromUserName = wxSubMsgResponseDTO.getFromUserName();
        wxMsgVO.CreateTime = wxSubMsgResponseDTO.getCreateTime();
        wxMsgVO.MsgType = wxSubMsgResponseDTO.getMsgType();
        wxMsgVO.Content = wxSubMsgResponseDTO.getContent();
        wxMsgVO.MsgId = wxSubMsgResponseDTO.getMsgId();
        return wxMsgVO;
    }
}