package com.ant.little.service.counter;

import com.alibaba.fastjson.JSON;
import com.ant.little.common.constents.KeyConfigTypeEnum;
import com.ant.little.common.constents.MemberTypeEnum;
import com.ant.little.common.model.Response;
import com.ant.little.model.dto.KeyConfigDTO;
import com.ant.little.model.dto.MemberInfoDTO;
import com.ant.little.model.dto.RequestCounterDTO;
import com.ant.little.service.model.UserInfo;
import com.ant.little.service.store.KeyConfigService;
import com.ant.little.service.store.MemberInfoService;
import com.ant.little.service.store.RequestCounterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: little-ant
 * 描述:
 * @date: 2022/12/26
 * @Version 1.0
 **/
@Service
public class RequestCounterAndLimitService {
    private Logger logger = LoggerFactory.getLogger(RequestCounterAndLimitService.class);
    @Autowired
    private RequestCounterService requestCounterService;
    @Autowired
    private MemberInfoService memberInfoService;
    @Autowired
    private KeyConfigService keyConfigService;
    private List<String> limitRequest = new ArrayList<>();

    @PostConstruct
    public void init() {
        limitRequest.add("FindPosition");
        limitRequest.add("GetLocalMap");
        limitRequest.add("MoriGameBestWay");
        limitRequest.add("MoriGameFindPath");
    }

    public Response limitCount(RequestCounterDTO requestCounterDTO, String serviceName) {
        if (!limitRequest.contains(serviceName)) {
            return Response.newSuccess("");
        }
        RequestCounterDTO requestCounterDTO1 = preCheck(requestCounterDTO);
        if (requestCounterDTO1.getRequestNum() >= requestCounterDTO1.getLimitNum()) {
            String msg = String.format("该功能今日已调用%d次,达到上限,请在小程序点击增加次数按钮增加。或明天再使用", requestCounterDTO1.getRequestNum());
            return Response.newFailure(msg, "");
        }
        return Response.newSuccess("");
    }

    public int addCount(RequestCounterDTO requestCounterDTO) {
        if (!limitRequest.contains(requestCounterDTO.getRequestKey())) {
            return 0;
        }
        return requestCounterService.addCount(requestCounterDTO);
    }


    private RequestCounterDTO preCheck(RequestCounterDTO requestCounterDTO) {
        RequestCounterDTO queryResult = requestCounterService.query(requestCounterDTO);
        if (queryResult != null) {
            return queryResult;
        }
        //无db信息读取会员信息 生成counter对象
        //无会员信息读取 使用系统配置生成一个counter对象
        MemberInfoDTO memberInfoDTO = new MemberInfoDTO();
        memberInfoDTO.setAppid(requestCounterDTO.getAppid());
        memberInfoDTO.setOpenId(requestCounterDTO.getOpenId());
        memberInfoDTO.setType(MemberTypeEnum.SERVICE_MEMBER.name());
        memberInfoDTO.setIsValid(1);
        MemberInfoDTO memberInfoResult = memberInfoService.query(memberInfoDTO);
        if (memberInfoResult != null && memberInfoResult.getEndTime().getTime() < System.currentTimeMillis()) {
            memberInfoService.inValidate(memberInfoResult);
            memberInfoResult = null;
        }
        if (memberInfoResult != null) {
            String json = memberInfoResult.getConfigJson();
            UserInfo userInfo = JSON.parseObject(json, UserInfo.class);
            if (userInfo.isVip()) {
                return createRequestCounter(true, requestCounterDTO);
            } else {
                return createRequestCounter(false, requestCounterDTO);
            }
        } else {
            return createRequestCounter(false, requestCounterDTO);
        }
    }

    private RequestCounterDTO createRequestCounter(boolean isVip, RequestCounterDTO requestCounterDTO) {
        String type = requestCounterDTO.getType();
        if (isVip) {
            type = KeyConfigTypeEnum.VIP.name();
        } else if (type.equals(KeyConfigTypeEnum.gh_d578112e1577.name())) {

        } else {
            type = KeyConfigTypeEnum.NORMAL.name();
        }
        Response<KeyConfigDTO> response = keyConfigService.getKey(type, requestCounterDTO.getRequestKey());
        requestCounterDTO.setRequestNum(0);
        if (response.getData() != null) {
            KeyConfigDTO keyConfigDTO = response.getData();
            int limit = Integer.parseInt(keyConfigDTO.getValue());
            requestCounterDTO.setLimitNum(limit);
        } else {
            //兜底10次
            requestCounterDTO.setLimitNum(10);
        }
        requestCounterService.insert(requestCounterDTO);
        return requestCounterService.query(requestCounterDTO);
    }


}
