package com.ant.little.model.dto;

import com.alibaba.fastjson.JSONObject;

import java.util.Date;

public class MemberConfigDTO {
    private Long id;

    private Date gmtCreate;

    private Date gmtModifier;

    private String env;

    private String appid;

    private String openId;

    private String type;

    private JSONObject configJson;

    private String configKey;

    private Integer isBand;

    private String bandOpenId;

    private String toUserName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId == null ? null : openId.trim();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public JSONObject getConfigJson() {
        return configJson;
    }

    public void setConfigJson(JSONObject configJson) {
        this.configJson = configJson;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey == null ? null : configKey.trim();
    }

    public Integer getIsBand() {
        return isBand;
    }

    public void setIsBand(Integer isBand) {
        this.isBand = isBand;
    }

    public String getBandOpenId() {
        return bandOpenId;
    }

    public void setBandOpenId(String bandOpenId) {
        this.bandOpenId = bandOpenId == null ? null : bandOpenId.trim();
    }

    public String getToUserName() {
        return toUserName;
    }

    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }
}