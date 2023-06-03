package com.ant.little.core.domain;

import java.util.Date;

public class MemberInfoDO {
    private Long id;

    private Date gmtCreate;

    private Date gmtModifier;

    private String env;

    private String appid;

    private String openId;

    private String type;

    private String configJson;

    private String bindConfigKey;

    private Date startTime;

    private Date endTime;

    private Integer isValid;

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

    public String getConfigJson() {
        return configJson;
    }

    public void setConfigJson(String configJson) {
        this.configJson = configJson == null ? null : configJson.trim();
    }

    public String getBindConfigKey() {
        return bindConfigKey;
    }

    public void setBindConfigKey(String bindConfigKey) {
        this.bindConfigKey = bindConfigKey == null ? null : bindConfigKey.trim();
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getIsValid() {
        return isValid;
    }

    public void setIsValid(Integer isValid) {
        this.isValid = isValid;
    }
}