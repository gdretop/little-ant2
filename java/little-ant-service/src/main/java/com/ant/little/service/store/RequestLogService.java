package com.ant.little.service.store;

import com.alibaba.fastjson.JSON;
import com.ant.little.common.model.Response;
import com.ant.little.core.config.EnvConfig;
import com.ant.little.core.dao.RequestLogDOMapper;
import com.ant.little.core.domain.RequestLogDO;
import com.ant.little.model.dto.RequestLogDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: little-ant
 * 描述:
 * @date: 2022/12/26
 * @Version 1.0
 **/
@Service
public class RequestLogService {
    private Logger logger = LoggerFactory.getLogger(RequestLogService.class);
    @Autowired
    private RequestLogDOMapper requestLogDOMapper;
    @Autowired
    private EnvConfig envConfig;

    public Response<RequestLogDTO> insert(RequestLogDTO requestLogDTO) {
        String env = envConfig.getCurEnv();
        requestLogDTO.setEnv(env);
        RequestLogDO requestLogDO = dto2DO(requestLogDTO);
//        int effect = requestLogDOMapper.insertSelective(requestLogDO);
//        if (effect == 1) {
            return Response.newSuccess(requestLogDTO);
//        } else {
//            logger.error("插入信息失败 {}", JSON.toJSONString(requestLogDTO));
//            return Response.newFailure("写入失败", "");
//        }
    }

    private RequestLogDO dto2DO(RequestLogDTO requestLogDTO) {
        RequestLogDO requestLogDO = new RequestLogDO();
        requestLogDO.setToUserName(requestLogDTO.getToUserName());
        requestLogDO.setFromUserName(requestLogDTO.getFromUserName());
        requestLogDO.setCreateTime(requestLogDTO.getCreateTime());
        requestLogDO.setMsgType(requestLogDTO.getMsgType());
        requestLogDO.setContent(requestLogDTO.getContent());
        requestLogDO.setWxSource(requestLogDTO.getWxSource());
        requestLogDO.setRealIp(requestLogDTO.getRealIp());
        requestLogDO.setMsgId(requestLogDTO.getMsgId());
        requestLogDO.setEvent(requestLogDTO.getEvent());
        requestLogDO.setEventKey(requestLogDTO.getEventKey());
        requestLogDO.setTicket(requestLogDTO.ticket);
        requestLogDO.setLatitude(requestLogDTO.getLatitude());
        requestLogDO.setLongitude(requestLogDTO.getLongitude());
        requestLogDO.setPrecision(requestLogDTO.getPrecision());
        requestLogDO.setOpenId(requestLogDTO.getOpenId());
        requestLogDO.setEnv(requestLogDTO.getEnv());
        requestLogDO.setAppid(requestLogDTO.getAppid());
        requestLogDO.setRequestInfo(requestLogDTO.getRequestInfo());
        requestLogDO.setResponseInfo(requestLogDTO.getResponseInfo());
        return requestLogDO;
    }

    public Response<RequestLogDTO> update(RequestLogDTO requestLogDTO) {
        RequestLogDO requestLogDO = dto2DO(requestLogDTO);
        int effect = requestLogDOMapper.updateByPrimaryKeySelective(requestLogDO);
        requestLogDTO.setId(requestLogDO.getId());
        if (effect == 1) {
            return Response.newSuccess(requestLogDTO);
        } else {
            logger.error("更新信息失败 {}", JSON.toJSONString(requestLogDTO));
            return Response.newFailure("更新失败", "");
        }
    }
}
