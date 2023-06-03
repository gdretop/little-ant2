package com.ant.little.model.dto;

import java.util.Date;

public class RequestLogDTO {
    private Long id;

    private String openId;

    private Date gmtCreate;

    private Date gmtModifier;

    private String env;

    private String appid;

    private String toUserName;
    private String fromUserName;
    private long createTime;
    private String msgType;
    private String content;
    private String wxSource;
    private String realIp;
    private long msgId;
    public String event;
    public String eventKey;
    public String ticket;
    public Double latitude;
    public Double longitude;
    public Double precision;

    private String requestInfo;

    private String responseInfo;

    public RequestLogDTO() {

    }

    public RequestLogDTO(WxSubMsgDTO wxSubMsgDTO) {
        setOpenId(wxSubMsgDTO.getWxOpenId());
        setAppid(wxSubMsgDTO.getWxAppid());
        this.toUserName = wxSubMsgDTO.getToUserName();
        this.fromUserName = wxSubMsgDTO.getFromUserName();
        this.createTime = wxSubMsgDTO.getCreateTime();
        this.msgType = wxSubMsgDTO.getMsgType();
//        this.content = wxSubMsgDTO.getContent();
        this.requestInfo = wxSubMsgDTO.getContent();
        this.wxSource = wxSubMsgDTO.getWxSource();
        this.realIp = wxSubMsgDTO.getRealIp();
        this.msgId = wxSubMsgDTO.getMsgId();
        this.event = wxSubMsgDTO.getEvent();
        this.eventKey = wxSubMsgDTO.getEventKey();
//        this.ticket = wxSubMsgDTO.getTicket();
        this.latitude = wxSubMsgDTO.getLatitude();
        this.longitude = wxSubMsgDTO.getLongitude();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId == null ? null : openId.trim();
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtModifier() {
        return gmtModifier;
    }

    public void setGmtModifier(Date gmtModifier) {
        this.gmtModifier = gmtModifier;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env == null ? null : env.trim();
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid == null ? null : appid.trim();
    }

    public String getRequestInfo() {
        return requestInfo;
    }

    public void setRequestInfo(String requestInfo) {
        this.requestInfo = requestInfo == null ? null : requestInfo.trim();
    }

    public String getResponseInfo() {
        return responseInfo;
    }

    public void setResponseInfo(String responseInfo) {
        this.responseInfo = responseInfo == null ? null : responseInfo.trim();
    }

    public String getToUserName() {
        return toUserName;
    }

    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public String getWxSource() {
        return wxSource;
    }

    public void setWxSource(String wxSource) {
        this.wxSource = wxSource;
    }

    public String getRealIp() {
        return realIp;
    }

    public void setRealIp(String realIp) {
        this.realIp = realIp;
    }


    public long getMsgId() {
        return msgId;
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getEventKey() {
        return eventKey;
    }

    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getPrecision() {
        return precision;
    }

    public void setPrecision(Double precision) {
        this.precision = precision;
    }
}