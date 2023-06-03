package com.ant.little.model.dto;

/**
 * @author: little-ant
 * 描述:
 * @date: 2022/12/27
 * @Version 1.0
 **/
public class WxSubMsgDTO implements Cloneable {
    private String toUserName;
    private String fromUserName;
    private long createTime;
    private String msgType;
    private String content;
    private String wxOpenId;
    private String wxSource;
    private String realIp;
    private String wxAppid;
    private long msgId;
    private String event;
    private String eventKey;
    private String ticket;
    private Double latitude;
    private Double longitude;
    private Double precision;

    public WxSubMsgResponseDTO toResponse() {
        WxSubMsgResponseDTO wxSubMsgResponseDTO = new WxSubMsgResponseDTO();
        wxSubMsgResponseDTO.setToUserName(this.getFromUserName());
        wxSubMsgResponseDTO.setFromUserName(this.getToUserName());
        wxSubMsgResponseDTO.setCreateTime(this.getCreateTime());
        wxSubMsgResponseDTO.setMsgId(this.getMsgId());
        return wxSubMsgResponseDTO;
    }

    public long getMsgId() {
        return msgId;
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
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

    public String getWxOpenId() {
        return wxOpenId;
    }

    public void setWxOpenId(String wxOpenId) {
        this.wxOpenId = wxOpenId;
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

    public String getWxAppid() {
        return wxAppid;
    }

    public void setWxAppid(String wxAppid) {
        this.wxAppid = wxAppid;
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
