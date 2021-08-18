package com.isport.tracker.entity;

public class sleepResultBean {
    public int startIndex;
    public int endIndex;
    public String startTime;
    public String endTime;

    @Override
    public String toString() {
        return "sleepResultBean{" +
                "startIndex=" + startIndex +
                ", endIndex=" + endIndex +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                '}';
    }
}
