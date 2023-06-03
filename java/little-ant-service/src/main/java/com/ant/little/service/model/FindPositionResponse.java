package com.ant.little.service.model;

import com.ant.little.common.model.Point;

/**
 * @author: little-ant
 * 描述:
 * @date: 2023/2/19
 * @Version 1.0
 **/
public class FindPositionResponse {
    private Point searchPosition = new Point();
    private String localMap = "";
    private boolean findMatch = false;
    private boolean hasNext = false;

    public Point getSearchPosition() {
        return searchPosition;
    }

    public void setSearchPosition(Point searchPosition) {
        this.searchPosition = searchPosition;
    }

    public String getLocalMap() {
        return localMap;
    }

    public void setLocalMap(String localMap) {
        this.localMap = localMap;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isFindMatch() {
        return findMatch;
    }

    public void setFindMatch(boolean findMatch) {
        this.findMatch = findMatch;
    }
}
