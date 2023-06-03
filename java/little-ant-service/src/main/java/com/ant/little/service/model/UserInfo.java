package com.ant.little.service.model;

/**
 * @author: little-ant
 * 描述:
 * @date: 2023/3/3
 * @Version 1.0
 **/
public class UserInfo {
    private int duration = 0;
    private boolean removeAd = false;
    private boolean vip = false;
    private boolean isAdmin = false;
    private int waitTime = 3000;

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isRemoveAd() {
        return removeAd;
    }

    public void setRemoveAd(boolean removeAd) {
        this.removeAd = removeAd;
    }

    public boolean isVip() {
        return vip;
    }

    public void setVip(boolean vip) {
        this.vip = vip;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }
}
