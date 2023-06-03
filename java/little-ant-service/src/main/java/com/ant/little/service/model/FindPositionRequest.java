package com.ant.little.service.model;

/**
 * @author: little-ant
 * 描述:
 * @date: 2023/2/19
 * @Version 1.0
 **/
public class FindPositionRequest {
    private String type;
    private int startX;
    private int startY;
    private int[][] matchData;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getStartX() {
        return startX;
    }

    public void setStartX(int startX) {
        this.startX = startX;
    }

    public int getStartY() {
        return startY;
    }

    public void setStartY(int startY) {
        this.startY = startY;
    }

    public int[][] getMatchData() {
        return matchData;
    }

    public void setMatchData(int[][] matchData) {
        this.matchData = matchData;
    }
}
