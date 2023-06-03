package com.ant.little.common.model;

/**
 * @author: little-ant
 * 描述:
 * @date: 2022/12/27
 * @Version 1.0
 **/
public class RunTimeResponse {
    /**
     * 0成功 其它失败
     */
    private int resultCode;
    /**
     * 返回结果
     */
    private String resultString;

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultString() {
        return resultString;
    }

    public void setResultString(String resultString) {
        this.resultString = resultString;
    }
}
