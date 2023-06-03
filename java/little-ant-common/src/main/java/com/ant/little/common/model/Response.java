package com.ant.little.common.model;

/**
 * @className: Response
 * @author: little-ant
 * @date: 2022/12/26
 **/
public class Response<T> {
    private boolean success;
    private T data;
    private String errMsg;
    private String errCode;

    public Response(T data) {
        this.data = data;
        this.success = true;
    }

    public Response(String errMsg, String errCode) {
        this.errCode = errCode;
        this.errMsg = errMsg;
        this.success = false;
    }

    public boolean isFailed() {
        return !success;
    }

    public static <D> Response<D> newSuccess(D data) {
        return new Response<D>(data);
    }

    public static <D> Response<D> newFailure(String errMsg, String errCode) {
        return new Response<>(errMsg, errCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }
}
