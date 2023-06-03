package com.ant.little.service.msganswer.answerimpl;

import com.alibaba.fastjson.JSON;
import com.ant.little.common.constents.ResponseTemplateConstants;
import com.ant.little.common.constents.WxMsgTypeEnum;
import com.ant.little.common.model.Point;
import com.ant.little.common.model.Response;
import com.ant.little.common.util.DigitalUtil;
import com.ant.little.model.dto.WxSubMsgDTO;
import com.ant.little.model.dto.WxSubMsgResponseDTO;
import com.ant.little.service.config.OldFunctionUserList;
import com.ant.little.service.findmap.FindMapWayUtil;
import com.ant.little.service.msganswer.MsgAnswerBaseService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author: little-ant
 * 描述:
 * @date: 2022/12/27
 * @Version 1.0
 **/
@Component
public class MoriGameBestWayAnswerService implements MsgAnswerBaseService {

    private final Logger logger = LoggerFactory.getLogger(MoriGameBestWayAnswerService.class);
    private Cache<String, String> localCache = CacheBuilder.newBuilder()
            .concurrencyLevel(4)
            .initialCapacity(100)
            .expireAfterWrite(3, TimeUnit.DAYS)
            .expireAfterAccess(3, TimeUnit.DAYS)
            .maximumSize(5000)
            .build();
    @Autowired
    private FindMapWayUtil findMapWayUtil;

    @Override
    public String getName() {
        return "MoriGameBestWay";
    }

    private final static String FORMAT_INFO = "正确格式示例如下,输入5行,第二行是当前坐标，3到5行是3个宝箱坐标,空格分割坐标:\n" +
            "最佳路线\n" +
            "202 213\n" +
            "59 189\n" +
            "86 229\n" +
            "114 166";
    ;

    @Override
    public boolean isMatch(WxSubMsgDTO wxSubMsgDTO) {
        if (!WxMsgTypeEnum.TEXT.getName().equals(wxSubMsgDTO.getMsgType())) {
            return false;
        }
        if (!wxSubMsgDTO.getContent().startsWith("最佳")) {
            return false;
        }
        String[] data = wxSubMsgDTO.getContent().split("\n");
        if (data.length != 5) {
            logger.error("格式不正确 {}", JSON.toJSONString(wxSubMsgDTO));
            throw new RuntimeException(FORMAT_INFO);
        }
        try {
            for (int i = 1; i < 4; i++) {
                data[i] = data[i].trim();
                while (data[i].contains("  ")) {
                    data[i] = data[i].replace("  ", " ");
                }
                Character split = DigitalUtil.findFirstNotDigit(data[i]);
                if (split == null || !(split.equals(' ') || split.equals(','))) {
                    logger.error("数字分隔符不正确 {}", JSON.toJSONString(wxSubMsgDTO));
                    throw new RuntimeException("坐标分隔符不正确请使用英文逗号(,)或空格");
                }
                int[] result = DigitalUtil.parseDigit(data[i], "" + split);
                if (result[0] < 1 || result[0] > 301 || result[1] < 1 || result[1] > 301) {
                    logger.error("坐标数字范围错误 {}", JSON.toJSONString(wxSubMsgDTO));
                    throw new RuntimeException("坐标数字范围1到301");
                }
            }
        } catch (Exception e) {
            logger.error("数据解析异常 {}", JSON.toJSONString(wxSubMsgDTO));
            if (e.getMessage().contains("坐标")) {
                throw new RuntimeException(e.getMessage());
            }
            throw new RuntimeException("数据格式有错." + FORMAT_INFO);
        }
        return true;
    }

    public Response<WxSubMsgResponseDTO> answer(WxSubMsgDTO wxSubMsgDTO) {
        try {
            if (!OldFunctionUserList.isAllow(wxSubMsgDTO)) {
                WxSubMsgResponseDTO wxSubMsgResponseDTO = wxSubMsgDTO.toResponse();
                wxSubMsgResponseDTO.setMsgType(WxMsgTypeEnum.TEXT.getName());
                wxSubMsgResponseDTO.setContent("请使用 菜单栏=>生存之路=>宝箱工具 ");
                return Response.newSuccess(wxSubMsgResponseDTO);
            }
            String content = wxSubMsgDTO.getContent();
            while (content.contains("  ")) {
                content = content.replace("  ", " ");
            }
            String[] data = content.split("\n");
            List<String> newData = new ArrayList<>();
            for (int i = 1; i < data.length; i++) {
                data[i] = data[i].trim();
                data[i] = data[i].replace(' ', ',');
                newData.add(data[i]);
            }
            content = String.join("#", newData);
            String cacheResult = localCache.getIfPresent(content);
            if (cacheResult != null) {
                logger.info("找到缓存信息");
                cacheResult = dataProcess(cacheResult, wxSubMsgDTO);
                WxSubMsgResponseDTO wxSubMsgResponseDTO = wxSubMsgDTO.toResponse();
                wxSubMsgResponseDTO.setMsgType(WxMsgTypeEnum.TEXT.getName());
                wxSubMsgResponseDTO.setContent(cacheResult);
                return Response.newSuccess(wxSubMsgResponseDTO);
            }
            List<Point> points = new ArrayList<>();
            for (int i = 1; i <= 4; i++) {
                String[] p = data[i].split(",");
                Point point = new Point(Integer.parseInt(p[0]), Integer.parseInt(p[1]));
                points.add(point);
            }
            List<String> resultList = findMapWayUtil.countBestWay(points);
            WxSubMsgResponseDTO wxSubMsgResponseDTO = wxSubMsgDTO.toResponse();
            wxSubMsgResponseDTO.setMsgType(WxMsgTypeEnum.TEXT.getName());
            String result = String.join("\n", resultList);
            localCache.put(content, result);
            result = dataProcess(result, wxSubMsgDTO);
            wxSubMsgResponseDTO.setContent(result);
            return Response.newSuccess(wxSubMsgResponseDTO);
        } catch (Exception e) {
            logger.error("处理失败 {} {}", JSON.toJSONString(wxSubMsgDTO), e.toString(), e);
            return Response.newFailure(ResponseTemplateConstants.SERVER_ERROR, "");
        }
    }

    private String dataProcess(String result, WxSubMsgDTO wxSubMsgDTO) {
        if ("gh_d578112e1577".equals(wxSubMsgDTO.getToUserName())) {
            result = "推荐使用小程序工具,菜单栏->生存之路->宝箱工具\n\n" + result;
        }
        result = result + "\n\n公众号:旺仔小蚂蚁";
        return result;
    }
}
