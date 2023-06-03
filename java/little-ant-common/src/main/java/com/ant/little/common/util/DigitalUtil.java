package com.ant.little.common.util;

import com.ant.little.common.model.Response;

/**
 * @author: little-ant
 * 描述:
 * @date: 2022/12/27
 * @Version 1.0
 **/
public class DigitalUtil {
    public static Response<int[][]> parsePoint(String content, int start, int end) {
        int[][] result = new int[end - start + 1][];
        String[] data = content.split("\n");
        for (int i = start; i <= end; i++) {
            result[i - start] = new int[2];
            data[i] = data[i].trim();
            while (data[i].contains("  ")) {
                data[i] = data[i].replace("  ", " ");
            }
            Character split = DigitalUtil.findFirstNotDigit(data[i]);
            if (split == null || !(split.equals(' ') || split.equals(','))) {
                return Response.newFailure("坐标分隔符不正确请使用英文逗号(,)或空格", "");
            }
            int[] parseResult = DigitalUtil.parseDigit(data[i], "" + split);
            if (parseResult[0] < 1 || parseResult[0] > 301 || parseResult[1] < 1 || parseResult[1] > 301) {
                return Response.newFailure("坐标数字范围1到301", "");
            }
            result[i - start] = parseResult;
        }
        return Response.newSuccess(result);
    }

    public static int[] parseDigit(String data, String split) {
        if (data == null) {
            return null;
        }
        String[] part = data.split(split);
        if (part.length != 2) {
            return null;
        }
        int a = Integer.parseInt(part[0]);
        int b = Integer.parseInt(part[1]);
        return new int[]{a, b};
    }

    public static Character findFirstNotDigit(String data) {
        for (int i = 0; i < data.length(); i++) {
            if (data.charAt(i) < '0' || data.charAt(i) > '9') {
                return data.charAt(i);
            }
        }
        return null;
    }
}
