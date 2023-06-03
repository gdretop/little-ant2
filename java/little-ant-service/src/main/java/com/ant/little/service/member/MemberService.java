package com.ant.little.service.member;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ant.little.common.constents.MemberTypeEnum;
import com.ant.little.common.model.Response;
import com.ant.little.common.util.DateUtil;
import com.ant.little.model.dto.KeyConfigDTO;
import com.ant.little.model.dto.MemberConfigDTO;
import com.ant.little.model.dto.MemberInfoDTO;
import com.ant.little.model.dto.RequestCounterDTO;
import com.ant.little.service.store.KeyConfigService;
import com.ant.little.service.store.MemberConfigService;
import com.ant.little.service.store.MemberInfoService;
import com.ant.little.service.store.RequestCounterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author: little-ant
 * 描述:
 * @date: 2022/12/26
 * @Version 1.0
 **/
@Service
public class MemberService {
    private Logger logger = LoggerFactory.getLogger(MemberService.class);
    @Autowired
    private MemberInfoService memberInfoService;
    @Autowired
    private MemberConfigService memberConfigService;
    @Autowired
    private RequestCounterService requestCounterService;

    /**
     * 创建会员配置
     *
     * @return
     */
    public Response<MemberConfigDTO> createMemberConfig(MemberConfigDTO memberConfigDTO) {
        return memberConfigService.insert(memberConfigDTO);
    }

    /**
     * 绑定会员
     * {
     * "isVip":false, 是否VIP
     * "removeAd":false, 移除广告
     * "duration":30  有效期
     * }
     *
     * @return
     */
    public Response bindMember(MemberConfigDTO memberConfigDTO) {
        String key = memberConfigDTO.getConfigKey();
        String type = memberConfigDTO.getType();
        MemberConfigDTO record = memberConfigService.query(type, key);
        if (record == null) {
            return Response.newFailure("查不到会员码信息", "");
        }
        if (record.getIsBand() != null && record.getIsBand() == 1) {
            return Response.newFailure("该会员码已绑定", "");
        }
        MemberInfoDTO memberInfoDTO = new MemberInfoDTO();
        memberInfoDTO.setAppid(memberConfigDTO.getAppid());
        memberInfoDTO.setOpenId(memberConfigDTO.getOpenId());
        memberInfoDTO.setType(memberConfigDTO.getType());
        {
            MemberInfoDTO memberRecord = memberInfoService.query(memberInfoDTO);
            if (memberRecord != null) {
                if (memberRecord.getEndTime().getTime() < System.currentTimeMillis()) {
                    memberInfoService.inValidate(memberRecord);
                } else {
                    logger.warn("当前会员的状态未失效,不可绑定 {}", JSON.toJSONString(memberConfigDTO));
                    return Response.newFailure("当前已绑定会员信息,请等失效后再绑定", "");
                }
            }
        }
        // 失效会员配置
        MemberConfigDTO updateConfig = new MemberConfigDTO();
        updateConfig.setId(record.getId());
        updateConfig.setIsBand(1);
        updateConfig.setBandOpenId(memberConfigDTO.getOpenId());
        Response response = memberConfigService.update(updateConfig);
        if (response.isFailed()) {
            return Response.newFailure("绑定会员信息失败", "");
        }
        // 生成会员信息
        JSONObject config = record.getConfigJson();
        memberInfoDTO.setConfigJson(JSON.toJSONString(config));
        memberInfoDTO.setBindConfigKey(memberConfigDTO.getConfigKey());
        Calendar now = new GregorianCalendar();
        memberInfoDTO.setStartTime(now.getTime());
        now.add(Calendar.DATE, config.getInteger("duration"));
        Date end = now.getTime();
        memberInfoDTO.setEndTime(end);
        memberInfoDTO.setIsValid(1);
        response = memberInfoService.insert(memberInfoDTO);
        String date = DateUtil.getDateString("yyyyMMdd");
        requestCounterService.deleteUserCounter(memberInfoDTO.getAppid(), memberInfoDTO.getOpenId(), date);
        return response;
    }
}
