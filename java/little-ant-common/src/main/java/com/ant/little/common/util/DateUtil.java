package com.ant.little.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author: little-ant
 * 描述:
 * @date: 2023/2/26
 * @Version 1.0
 **/
public class DateUtil {
    public static String getDateString(String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }
}
