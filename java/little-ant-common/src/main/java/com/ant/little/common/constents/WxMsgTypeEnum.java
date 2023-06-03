package com.ant.little.common.constents;

/**
 * @author: little-ant
 * 描述:
 * @date: 2022/12/27
 * @Version 1.0
 **/
public enum WxMsgTypeEnum {
    TEXT("text"),
    JSON("json"),
    EVENT("event");
    private String name;

    WxMsgTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
