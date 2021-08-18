package com.isport.tracker.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/10.
 * 从设备获取到的数据统计
 */

public class HeartDataInfo implements Serializable {
    private int max;
    private int min;
    private int avg;
    private int total;
    private long startTime;///心率测量开始时间
    private long currentTime;///当前时间
    private long totalTime;///心率测量时间
    private long totalCal;////消耗的热量(cal)
    private List<Integer> dataList;

    public HeartDataInfo(){
        this.dataList = new ArrayList<>();
        max = 0;
        min = 0;
        avg = 0;
        total = 0;
        startTime = 0;
        totalTime = 0;
        totalCal = 0;
        currentTime = 0;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public int getMax() {
        return max;
    }

    public int getMin() {
        return min;
    }

    public int getAvg() {
        return avg;
    }

    public List<Integer> getDataList() {
        return dataList;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public void setAvg(int avg) {
        this.avg = avg;
    }

    public void setDataList(List<Integer> dataList) {
        this.dataList = dataList;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }


    public long getTotalCal() {
        return totalCal;
    }

    public void setTotalCal(long totalCal) {
        this.totalCal = totalCal;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
        long dtime = (currentTime - startTime)/1000;
        this.totalTime = dtime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }
}
