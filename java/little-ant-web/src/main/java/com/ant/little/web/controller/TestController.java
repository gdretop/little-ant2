package com.ant.little.web.controller;

import com.alibaba.fastjson.JSON;
import com.ant.little.web.config.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping(value = "/api/test")
public class TestController {

    final Logger logger = LoggerFactory.getLogger(TestController.class);
    @Resource
    private HttpServletRequest httpServletRequest;

    public TestController() {
    }


    @PostMapping(value = "/output")
    @ResponseBody
    public ApiResponse output(@RequestBody Map<String, Object> requestMap) {
//        x-wx-openid
//        x-wx-source
//        x-real-ip
//        x-wx-appid
        httpServletRequest.getHeaderNames();
        Map<String, String> map = new HashMap<String, String>();
        Enumeration headerNames = httpServletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = httpServletRequest.getHeader(key);
            map.put(key, value);
        }
        logger.info("接收到测试请求:openId:{} source:{} data:{}", map.get("x-wx-from-openid"), map.get("x-wx-source"), requestMap);
        logger.info("header信息: {}", JSON.toJSONString(map));
        logger.info("输入信息: {}", JSON.toJSONString(requestMap));
        Map<String, Object> response = new HashMap<>();
        response.put("header", map);
        response.put("body", requestMap);
        return ApiResponse.ok(response);
    }

    @PostMapping(value = "/received")
    @ResponseBody
    public Map<String, Object> received(@RequestBody Map<String, Object> requestMap) {
//        x-wx-openid
//        x-wx-source
//        x-real-ip
//        x-wx-appid
        httpServletRequest.getHeaderNames();
        Map<String, String> map = new HashMap<String, String>();
        Enumeration headerNames = httpServletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = httpServletRequest.getHeader(key);
            map.put(key, value);
        }
        logger.info("接收到测试请求:openId:{} source:{} data:{}", map.get("x-wx-from-openid"), map.get("x-wx-source"), requestMap);
        logger.info("header信息: {}", JSON.toJSONString(map));
        logger.info("输入信息: {}", JSON.toJSONString(requestMap));
//        requestMap.put("header", map);
        return requestMap;
    }

    @PostMapping(value = "/answer")
    @ResponseBody
    public Map<String, Object> answer(@RequestBody Map<String, Object> requestMap) {
//        ToUserName: FromUserName,
//                FromUserName: ToUserName,
//                CreateTime: CreateTime,
//                MsgType: 'text',
//                Content: '这是回复的消息'
        httpServletRequest.getHeaderNames();
        Map<String, String> map = new HashMap<String, String>();
        Enumeration headerNames = httpServletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = httpServletRequest.getHeader(key);
            map.put(key, value);
        }
        Object ToUserName = requestMap.get("ToUserName");
        Object FromUserName = requestMap.get("FromUserName");
        requestMap.put("FromUserName", ToUserName);
        requestMap.put("ToUserName", FromUserName);
        logger.info("接收到测试请求:openId:{} source:{} data:{}", map.get("x-wx-from-openid"), map.get("x-wx-source"), requestMap);
        logger.info("header信息: {}", JSON.toJSONString(map));
        logger.info("输入信息: {}", JSON.toJSONString(requestMap));
//        requestMap.put("header", map);
        return requestMap;
    }

}