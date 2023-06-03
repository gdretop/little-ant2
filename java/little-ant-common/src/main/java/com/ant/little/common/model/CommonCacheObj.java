package com.ant.little.common.model;

/**
 * @author: little-ant
 * 描述:
 * @date: 2023/2/26
 * @Version 1.0
 **/
public class CommonCacheObj<T> {
    public T data = null;

    public CommonCacheObj(T data) {
        this.data = data;
    }

    public CommonCacheObj() {

    }
}
