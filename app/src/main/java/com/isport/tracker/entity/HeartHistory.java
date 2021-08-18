package com.isport.tracker.entity;

import com.isport.isportlibrary.entry.HeartData;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016/11/7.
 */

public class HeartHistory implements Serializable{
    public final static int TYPE_NORMAL = 1;//普通
    public final static int TYPE_SPORT = 2;///运动
    public final static int TYPE_SLEEP = 3;///睡觉
    public final static int TYPE_RESTING = 4;///休息
    public final static int TYPE_AUTO = 6;///自动保存

    private int type;
    private String mac;
    private String startDate;///开始日期
    private ArrayList<HeartData> heartDataList;
    private int avg;
    private int max;
    private int min;
    private int size;///心率总个数
    private long total;///心率总数
    private long totalCal;////总热量，仅适用于心率带
    private boolean isSelected;


    public int getIsHistory() {
        return isHistory;
    }

    public void setIsHistory(int isHistory) {
        this.isHistory = isHistory;
    }

    private int isHistory;

    public HeartHistory(int type, String mac, String startDate, ArrayList<HeartData> list, int avg, int max, int min, long total, long totalCal,int isHistory){
        this.mac = mac;
        this.type = type;
        this.startDate = startDate;
        this.heartDataList = list;
        this.avg = avg;
        this.max = max;
        this.min = min;
        this.total = total;
        this.totalCal = totalCal;
        this.size = (list == null?0:list.size());
        this.isHistory =isHistory;
    }

    public long getTotal() {
        return total;
    }

    public int getSize() {
        return size;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public int getType() {
        return type;
    }

    public String getStartDate() {
        return startDate;
    }

    public ArrayList<HeartData> getHeartDataList() {
        return heartDataList;
    }

    public int getAvg() {
        return avg;
    }

    public int getMax() {
        return max;
    }

    public int getMin() {
        return min;
    }

    public String getMac() {
        return mac;
    }

    public long getTotalCal() {
        return totalCal;
    }

    public void setTotalCal(long totalCal) {
        this.totalCal = totalCal;
    }
}
