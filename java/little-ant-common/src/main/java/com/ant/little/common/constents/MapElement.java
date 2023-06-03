package com.ant.little.common.constents;

import com.alibaba.fastjson.JSONObject;

/**
 * @author: little-ant
 * 描述:
 * @date: 2023/1/25
 * @Version 1.0
 **/
public class MapElement {
    private static final String typeValue = "{\n" +
            "        \"0\":  { 'target_color': '00B050', 'color': '00B050', \"useful\": false, \"reach\": false, \"name\": \"草地岩石\"},\n" +
            "        \"1\":  { 'target_color': '92D050', 'color': '92D050', \"useful\": false, \"reach\": false, \"name\": \"栏栅\"},\n" +
            "        \"2\":  { 'target_color': 'FFFFFF', 'color': 'F0F0F0', \"useful\": false, \"reach\": true , \"name\": \"空地\"},\n" +
            "        \"3\":  { 'target_color': '7030A0', 'color': '7030A0', \"useful\": true,  \"reach\": true , \"name\": \"山洞\"},\n" +
            "        \"4\":  { 'target_color': 'F196AB', 'color': 'F196AB', \"useful\": true,  \"reach\": true , \"name\": \"坐标牌\"},\n" +
            "        \"5\":  { 'target_color': 'FF922B', 'color': 'FF922B', \"useful\": true,  \"reach\": true , \"name\": \"建筑\"},\n" +
            "        \"6\":  { 'target_color': '00B0F0', 'color': '00B0F0', \"useful\": true,  \"reach\": true , \"name\": \"商店\"},\n" +
            "        \"7\":  { 'target_color': '817709', 'color': '817709', \"useful\": true,  \"reach\": true , \"name\": \"医院\"},\n" +
            "        \"8\":  { 'target_color': 'FFC000', 'color': 'FFC000', \"useful\": true,  \"reach\": true , \"name\": \"教堂\"},\n" +
            "        \"9\":  { 'target_color': 'FF0000', 'color': 'FF0000', \"useful\": true,  \"reach\": true , \"name\": \"加油站\"},\n" +
            "        \"10\": { 'target_color': '002060', 'color': '002060', \"useful\": true,  \"reach\": true , \"name\": \"广场\"},\n" +
            "        \"11\": { 'target_color': 'C00000', 'color': 'C00000', \"useful\": true,  \"reach\": true , \"name\": \"帐篷\"},\n" +
            "        \"12\": { 'target_color': '4290FF', 'color': '4290FF', \"useful\": true,  \"reach\": true , \"name\": \"工厂\"},\n" +
            "        \"13\": { 'target_color': 'A219FF', 'color': 'A219FF', \"useful\": true,  \"reach\": true , \"name\": \"房屋\"},\n" +
            "        \"14\": { 'target_color': '598F38', 'color': '598F38', \"useful\": false, \"reach\": false, \"name\": \"热带树\"},\n" +
            "        \"15\": { 'target_color': '707070', 'color': 'E5F3F1', \"useful\": false, \"reach\": false, \"name\": \"石头\"},\n" +
            "        \"16\": { 'target_color': '355522', 'color': '355522', \"useful\": false, \"reach\": false, \"name\": \"深绿树\"},\n" +
            "        \"17\": { 'target_color': 'BEFF9E', 'color': 'BEFF9E', \"useful\": false, \"reach\": false, \"name\": \"带花草\"},\n" +
            "        \"18\": { 'target_color': '89FF8B', 'color': '89FF8B', \"useful\": false, \"reach\": false, \"name\": \"小草\"},\n" +
            "        \"19\": { 'target_color': 'FFFFFF', 'color': 'FAFF9F', \"useful\": false, \"reach\": true , \"name\": \"标记空地\"},\n" +
            "    }";
    public static final JSONObject json = JSONObject.parseObject(typeValue);

    public static int[][] DIR = {
            {0, 1}, {0, -1}, {1, 0}, {-1, 0}
    };

    public static JSONObject getTypeMap() {
//        JSONObject json = JSON.parseObject(typeValue);
        return json;
    }

    public static JSONObject getElement(String key) {
        return getTypeMap().getJSONObject(key);
    }

    public static JSONObject getElement(int key) {
        return getTypeMap().getJSONObject("" + key);
    }
}
