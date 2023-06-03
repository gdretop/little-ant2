package com.ant.little.service.msganswer;

import com.alibaba.fastjson.JSON;
import com.ant.little.common.constents.ResponseTemplateConstants;
import com.ant.little.common.model.Response;
import com.ant.little.common.util.DateUtil;
import com.ant.little.model.dto.*;
import com.ant.little.service.counter.RequestCounterAndLimitService;
import com.ant.little.service.msganswer.answerimpl.*;
import com.ant.little.service.store.RequestLogService;
import com.ant.little.service.store.WxUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: little-ant
 * 描述:
 * @date: 2022/12/27
 * @Version 1.0
 **/
@Service
public class MsgAnswerRouter {
    private final Logger logger = LoggerFactory.getLogger(MsgAnswerRouter.class);
    private List<MsgAnswerBaseService> msgAnswerBaseServiceList = new ArrayList<>();
    @Autowired
    private RequestLogService requestLogService;
    @Autowired
    private WxUserService wxUserService;
    @Autowired
    private RequestCounterAndLimitService requestRequestCounterAndLimitService;

    public MsgAnswerRouter(@Autowired MoriGameBestWayAnswerService moriGameWayPathAnswerService,
                           @Autowired MoriGameFindPathAnswerService moriGameFindPathAnswerService,
                           @Autowired SubscribeService subscribeService,
                           @Autowired UpdateBoxPositionAnswerService updateBoxPositionAnswerService,
                           @Autowired GetKeyConfigAnswerService getKeyConfigAnswerService,
                           @Autowired GetLocalMapService getLocalMapService,
                           @Autowired FindPositionService findPositionService,
                           @Autowired BindMemberAnswerService bindMemberAnswerService,
                           @Autowired CreateMemberAnswerService createMemberAnswerService,
                           @Autowired QueryMemberConfigAnswerService queryMemberConfigAnswerService,
                           @Autowired UpdateConfigAnswerService updateConfigAnswerService,
                           @Autowired QueryUserInfoAnswerService queryUserInfoAnswerService,
                           @Autowired AddRequestNumAnswerService addRequestNumAnswerService,
                           @Autowired ClearCacheService clearCacheService) {
        msgAnswerBaseServiceList.add(addRequestNumAnswerService);
        msgAnswerBaseServiceList.add(updateConfigAnswerService);
        msgAnswerBaseServiceList.add(moriGameWayPathAnswerService);
        msgAnswerBaseServiceList.add(moriGameFindPathAnswerService);
        msgAnswerBaseServiceList.add(subscribeService);
        msgAnswerBaseServiceList.add(updateBoxPositionAnswerService);
        msgAnswerBaseServiceList.add(getKeyConfigAnswerService);
        msgAnswerBaseServiceList.add(getLocalMapService);
        msgAnswerBaseServiceList.add(findPositionService);
        msgAnswerBaseServiceList.add(bindMemberAnswerService);
        msgAnswerBaseServiceList.add(createMemberAnswerService);
        msgAnswerBaseServiceList.add(queryMemberConfigAnswerService);
        msgAnswerBaseServiceList.add(queryUserInfoAnswerService);
        msgAnswerBaseServiceList.add(clearCacheService);
    }

    public Response<WxSubMsgResponseDTO> process(WxSubMsgDTO wxSubMsgDTO) {
        WxUserDTO wxUserDTO = new WxUserDTO();
        try {
            wxUserDTO.setOpenId(wxSubMsgDTO.getWxOpenId());
            wxUserDTO.setAppid(wxSubMsgDTO.getWxAppid());
            wxUserDTO.setLastLocation(wxSubMsgDTO.getRealIp());
            wxUserDTO.setLastSource(wxSubMsgDTO.getWxSource());
            wxUserService.upsert(wxUserDTO);
        } catch (Exception e) {
            logger.error("保存用户信息失败 {} {}", JSON.toJSONString(wxUserDTO), e, e);
        }

        RequestLogDTO requestLogDTO = new RequestLogDTO(wxSubMsgDTO);
        Response<WxSubMsgResponseDTO> responseAnswer = null;
        try {
            for (MsgAnswerBaseService service : msgAnswerBaseServiceList) {
                boolean matchResult = false;
                try {
                    matchResult = service.isMatch(wxSubMsgDTO);
                } catch (Exception e) {
                    logger.error("匹配抛出异常 {}", e.toString());
                    return Response.newFailure(e.getMessage(), "");
                }
                if (matchResult) {
                    // 成功调用次数计数器
                    RequestCounterDTO requestCounterDTO = new RequestCounterDTO();
                    requestCounterDTO.setAppid(wxSubMsgDTO.getWxAppid());
                    requestCounterDTO.setOpenId(wxSubMsgDTO.getWxOpenId());
                    requestCounterDTO.setType(wxSubMsgDTO.getToUserName());
                    requestCounterDTO.setRequestKey(service.getName());
                    requestCounterDTO.setBizDate(DateUtil.getDateString("yyyyMMdd"));
                    Response limitResponse = requestRequestCounterAndLimitService.limitCount(requestCounterDTO, service.getName());
                    if (limitResponse.isFailed()) {
                        return limitResponse;
                    }
                    responseAnswer = service.answer(wxSubMsgDTO);
                    if (responseAnswer.isSuccess()) {
                        requestRequestCounterAndLimitService.addCount(requestCounterDTO);
                    }
                    return responseAnswer;
                }
            }
            responseAnswer = Response.newFailure(ResponseTemplateConstants.CAN_NOT_FIND_ANSWER_SERVICE, "");
            return responseAnswer;
        } finally {
            try {
                logger.info("保存记录日志");
                if (responseAnswer != null && responseAnswer.isSuccess() && !responseAnswer.getData().isSaveResponse()) {

                } else {
                    requestLogDTO.setResponseInfo(JSON.toJSONString(responseAnswer));
                }
                requestLogService.insert(requestLogDTO);
            } catch (Exception e) {
                logger.error("保存日志信息失败 {} {}", JSON.toJSONString(requestLogDTO), e, e);
            }
        }
    }
}
