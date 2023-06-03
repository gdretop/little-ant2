package com.ant.little.common.model;

/**
 * @author: little-ant
 * 描述:
 * @date: 2023/1/25
 * @Version 1.0
 **/
public class Point {
    public int x;
    public int y;
    public int value;
    public int dist;
    public int buildings;
    public int id;

    public Point copy() {
        Point np = new Point();
        np.x = this.x;
        np.y = this.y;
        np.value = this.value;
        np.dist = this.dist;
        np.buildings = this.buildings;
        np.id = this.id;
        return np;
    }

    public Point() {

    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getKey() {
        return x * 400 + y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getDist() {
        return dist;
    }

    public void setDist(int dist) {
        this.dist = dist;
    }

    public int getBuildings() {
        return buildings;
    }

    public void setBuildings(int buildings) {
        this.buildings = buildings;
    }
}
