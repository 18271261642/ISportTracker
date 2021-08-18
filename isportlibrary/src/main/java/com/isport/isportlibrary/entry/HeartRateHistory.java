package com.isport.isportlibrary.entry;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @Author
 * @Date 2018/11/14
 * @Fuction
 */

public class HeartRateHistory implements Serializable {

    private String mac;
    private int count;///有效数据的长度
    private String startDate;///开始日期
    private int avg;
    private int max;
    private int min;
    private ArrayList<HeartRateData> heartDataList;

    public ArrayList<HeartRateData> getHeartDataList() {
        return heartDataList;
    }

    public void setHeartDataList(ArrayList<HeartRateData> heartDataList) {
        this.heartDataList = heartDataList;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public HeartRateHistory() {

    }

    public HeartRateHistory(String mac, int count, String startDate, int avg, int max, int min,
                            ArrayList<HeartRateData> heartDataList) {
        this.mac = mac;
        this.count = count;
        this.startDate = startDate;
        this.avg = avg;
        this.max = max;
        this.min = min;
        this.heartDataList = heartDataList;
    }

    @Override
    public String toString() {
        return "HeartRateHistory{" +
                "mac='" + mac + '\'' +
                ", count=" + count +
                ", startDate='" + startDate + '\'' +
                ", avg=" + avg +
                ", max=" + max +
                ", min=" + min +
                ", heartDataList=" + heartDataList +
                '}';
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public int getAvg() {
        return avg;
    }

    public void setAvg(int avg) {
        this.avg = avg;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }
}
